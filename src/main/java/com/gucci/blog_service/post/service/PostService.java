package com.gucci.blog_service.post.service;

import com.gucci.blog_service.comment.service.CommentRefService;
import com.gucci.blog_service.config.JwtTokenHelper;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.repository.PostDocRepository;
import com.gucci.blog_service.post.repository.PostRepository;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostDocRepository postDocRepository;


    private final CommentRefService commentRefService;

    private final JwtTokenHelper jwtTokenHelper;

    /**
     * 게시글
     */
    @Transactional
    public Post createPost(String token, PostRequestDTO.createPost dto) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        //임시저장 글이었을 경우
        if (dto.getPostId() != null) {
            Post post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
            post.publish();

            return post;
        }


        // 새로 작성한 글인 경우
        PostDocument postDocument = PostDocument.builder()
                .content(dto.getContent())
                .build();
        postDocRepository.save(postDocument);

        Post post = Post.builder()
                .view(0L)
                .documentId(postDocument.getId())
                .userId(userId)
                .title(dto.getTitle())
                .isDraft(false)
                .build();

        return postRepository.save(post);
    }

    public PostResponseDTO.GetPostDetail getPostDetail(Long postId) {
        Post post = postRepository.findById(postId).
                orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); //todo : NOT_FOUND_POST_CONTENT

        return PostResponseDTO.GetPostDetail.builder()
                .postId(post.getPostId())
                .authorId(post.getUserId())
                .authorNickname("임시")
                .view(post.getView())
                .title(post.getTitle())
                .content(postDocument.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Transactional
    public Post updatePost(String token, PostRequestDTO.updatePost dto) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);


        Post post;
        PostDocument postDocument;
        //임시저장 글이었을 경우
        if (dto.getParentPostId() != null){
            post = postRepository.findById(dto.getParentPostId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
            postDocument = postDocRepository.findById(post.getDocumentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); // todo : NOT_FOUND_POST_CONTENT
        }
        else { // 바로 수정할 경우
            post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
            postDocument = postDocRepository.findById(post.getDocumentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); // todo : NOT_FOUND_POST_CONTENT
        }

        //권한 체크. 글 작성자만 수정 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        // 임시저장 글이었을 경우 임시저장 글 삭제
        // 권한 체크 후 삭제 진행하는게 맞다고 판단돼서 여기에 위치함
        if (dto.getParentPostId() != null){
            Post draft = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
            PostDocument draftDocument = postDocRepository.findById(post.getDocumentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); // todo : NOT_FOUND_POST_CONTENT
            postRepository.delete(draft);
            postDocRepository.delete(draftDocument);
        }

        postDocument.updateContent(dto.getContent());
        postDocRepository.save(postDocument); // 도큐먼트를 추적해서 변경된 필드를 저장하는 구조가 아니기 때문에, 반드시 save()를 직접 호출해야 반영

        post.updateTitle(dto.getTitle());
        return post;
    }

    @Transactional
    public void deletePost(String token, Long postId) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        //권한 체크. 글 작성자만 삭제 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        //임시저장 글 삭제
        Post draft = postRepository.findByParentPostId(postId).orElse(null);
        if (draft != null) {
            PostDocument draftDoc = postDocRepository.findById(draft.getDocumentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
            postRepository.delete(draft);
            postDocRepository.delete(draftDoc);
        }

        //글 삭제
        commentRefService.deleteAllByPost(post);
        postRepository.delete(post);
        postDocRepository.delete(postDocument);
    }


    /**
     * 임시저장글
     */
    public Post createDraft(String token, PostRequestDTO.createDraft dto) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        PostDocument postDocument = PostDocument.builder()
                .content(dto.getContent())
                .build();

        postDocRepository.save(postDocument);
        Post post = Post.builder()
                .view(0L)
                .documentId(postDocument.getId())
                .userId(userId)
                .title(dto.getTitle())
                .isDraft(true)
                .build();

        return postRepository.save(post);
    }

    public PostResponseDTO.GetDraftDetail getDraftDetail(String token, Long postId) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

        if (!post.getUserId().equals(userId)) { //사용자 != 임시저장 작성자
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        return PostResponseDTO.GetDraftDetail.builder()
                .postId(post.getPostId())
                .authorId(post.getUserId())
                .title(post.getTitle())
                .authorNickname("임시")
                .content(postDocument.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public PostResponseDTO.GetDraftList getDraftList(String token) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        List<Post> postList = postRepository.findAllByUserId(userId);

        List<PostResponseDTO.GetDraftDetail> draftList = postList.stream().map(post -> {
                PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

                return PostResponseDTO.GetDraftDetail.builder()
                        .postId(post.getPostId())
                        .title(post.getTitle())
                        .authorId(post.getUserId())
                        .authorNickname("임시")
                        .content(postDocument.getContent())
                        .updatedAt(post.getUpdatedAt())
                        .createdAt(post.getCreatedAt())
                        .build();
            }
        ).toList();

        return PostResponseDTO.GetDraftList.builder()
                .draftList(draftList)
                .build();
    }

    public void deleteDraft(String token, Long postId) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        //권한 체크. 글 작성자만 삭제 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        postRepository.delete(post);
        postDocRepository.delete(postDocument);
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.INVALID_ARGUMENT) //todo : NOT_FOUND_POST
        );
    }
}