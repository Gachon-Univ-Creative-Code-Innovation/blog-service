package com.gucci.blog_service.post.service;

import com.gucci.blog_service.comment.service.CommentRefService;
import com.gucci.blog_service.config.JwtTokenHelper;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.repository.PostDocRepository;
import com.gucci.blog_service.post.repository.PostRepository;
import com.gucci.blog_service.tag.service.TagService;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostDocRepository postDocRepository;


    private final CommentRefService commentRefService;
    private final TagService tagService;

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
            PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

            //태그 업데이트
            tagService.updateByTagNameList(post, dto.getTagNameList());

            //postDoc 업데이트
            postDocument.updateContent(dto.getContent());
            postDocRepository.save(postDocument);

            //post 업데이트
            post.publish(dto.getTitle());
            return postRepository.save(post);
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
        Post savedPost = postRepository.save(post);

        //태그 생성, 이때 post 객체가 DB에서 조회된 영속 상태여야함
        tagService.createTags(savedPost, dto.getTagNameList());

        return savedPost;
    }


    public PostResponseDTO.GetPostDetail getPostDetail(Long postId) {
        Post post = postRepository.findById(postId).
                orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); //todo : NOT_FOUND_POST_CONTENT
        List<String> tagNameList = tagService.getTagNamesByPost(post);

        return PostResponseDTO.GetPostDetail.builder()
                .postId(post.getPostId())
                .authorId(post.getUserId())
                .authorNickname("임시")
                .view(post.getView())
                .title(post.getTitle())
                .content(postDocument.getContent())
                .tagNameList(tagNameList)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }


    @Transactional
    public Post updatePost(String token, Long postId, PostRequestDTO.updatePost dto) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post;
        PostDocument postDocument;

        post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        postDocument = postDocRepository.findById(post.getDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); // todo : NOT_FOUND_POST_CONTENT


        //권한 체크. 글 작성자만 수정 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        // 1. 임시저장 글이 있을 경우 삭제
        Post draft = postRepository.findByParentPostId(postId).orElse(null);
        if (draft != null) {
            PostDocument draftDocument = postDocRepository.findById(post.getDocumentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); // todo : NOT_FOUND_POST_CONTENT
            postRepository.delete(draft);
            postDocRepository.delete(draftDocument);
        }

        //Doc 업데이트
        postDocument.updateContent(dto.getContent());
        postDocRepository.save(postDocument); // 도큐먼트를 추적해서 변경된 필드를 저장하는 구조가 아니기 때문에, 반드시 save()를 직접 호출해야 반영

        //tag 업데이트
        tagService.updateByTagNameList(post, dto.getTagNameList());

        //Post 업데이트
        post.updateTitle(dto.getTitle());
        return post;
    }


    @Transactional
    public void deletePost(String token, Long postId) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));// todo : NOT_FOUND_POST_CONTENT
        //권한 체크. 글 작성자만 삭제 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        //임시저장 글 삭제
        Post draft = postRepository.findByParentPostId(postId).orElse(null);
        if (draft != null) {
            PostDocument draftDoc = postDocRepository.findById(draft.getDocumentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));// todo : NOT_FOUND_POST_CONTENT
            tagService.deleteAllByPost(draft);
            postRepository.delete(draft);
            postDocRepository.delete(draftDoc);
        }

        //댓글, 태그, Doc, Post 삭제
        tagService.deleteAllByPost(post);
        commentRefService.deleteAllByPost(post);
        postRepository.delete(post);
        postDocRepository.delete(postDocument);
    }


    /**
     * 임시저장글
     */
    @Transactional
    public Post createDraft(String token, PostRequestDTO.createDraft dto) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        //글 발행 전 임시저장
        if (dto.getDraftPostId() == null && dto.getParentPostId() == null){
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
            Post savedPost = postRepository.save(post);

            //태그저장
            tagService.createTags(savedPost, dto.getTagNameList());

            return savedPost;
        }
        // 글 발행 후 임시저장
        else if (dto.getDraftPostId() == null){
            PostDocument postDocument = PostDocument.builder()
                    .content(dto.getContent())
                    .build();
            postDocRepository.save(postDocument);

            Post post = Post.builder()
                    .view(0L)
                    .parentPostId(dto.getParentPostId())
                    .documentId(postDocument.getId())
                    .userId(userId)
                    .title(dto.getTitle())
                    .isDraft(true)
                    .build();
            Post savedPost = postRepository.save(post);

            tagService.createTags(savedPost, dto.getTagNameList());
            return savedPost;
        }
        // 임시저장 글을 또 임시저장
        else {
            Post draft = postRepository.findById(dto.getDraftPostId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
            PostDocument draftDoc = postDocRepository.findById(draft.getDocumentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); // todo : NOT_FOUND_POST_CONTENT

            tagService.updateByTagNameList(draft, dto.getTagNameList());
            draftDoc.updateContent(dto.getContent());
            postDocRepository.save(draftDoc); // 도큐먼트를 추적해서 변경된 필드를 저장하는 구조가 아니기 때문에, 반드시 save()를 직접 호출해야 반영
            draft.updateTitle(dto.getTitle());

            return draft;
        }
    }


    public PostResponseDTO.GetDraftDetail getDraftDetail(String token, Long postId) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); // todo : NOT_FOUND_POST_CONTENT

        if (!post.isDraft()) { // 임시저장 글이 아님
            throw new CustomException(ErrorCode.INVALID_ARGUMENT);
        }
        if (!post.getUserId().equals(userId)) { //사용자 != 임시저장 작성자
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        List<String> tagNameList = tagService.getTagNamesByPost(post);

        return PostResponseDTO.GetDraftDetail.builder()
                .draftPostId(post.getPostId())
                .parentPostId(post.getParentPostId())
                .authorId(post.getUserId())
                .title(post.getTitle())
                .authorNickname("임시")
                .content(postDocument.getContent())
                .tagNameList(tagNameList)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }



    public PostResponseDTO.GetDraftList getDraftList(String token) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        List<Post> postList = postRepository.findAllByUserId(userId);
        List<String> draftDocIds = postList.stream()
                .filter(Post::isDraft)
                .map(Post::getDocumentId)
                .toList();
        Map<String, PostDocument> postDocMap = postDocRepository.findAllById(draftDocIds).stream()
                .collect(Collectors.toMap(PostDocument::getId, Function.identity())); // Function.identity() : 받은 값을 그대로 return


        List<PostResponseDTO.GetDraft> draftList = postList.stream()
                .filter(Post::isDraft) // draft만 통과
                .map(post -> {
                            PostDocument postDocument = postDocMap.get(post.getDocumentId());
                            if (postDocument == null) {
                                throw new CustomException(ErrorCode.NOT_FOUND_POST);
                            }

                            return PostResponseDTO.GetDraft.builder()
                                    .draftPostId(post.getPostId())
                                    .title(post.getTitle())
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