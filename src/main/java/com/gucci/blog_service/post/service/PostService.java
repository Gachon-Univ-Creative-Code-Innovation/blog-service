package com.gucci.blog_service.post.service;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.service.CommentRefService;
import com.gucci.blog_service.comment.service.CommentService;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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

        PostDocument postDocument = PostDocument.builder()
                .content(dto.getContent())
                .build();

        postDocRepository.save(postDocument);
        Post post = Post.builder()
                .view(0L)
                .documentId(postDocument.getId())
                .userId(userId)
                .title(dto.getTitle())
                .isTemp(false)
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
    public Post updatePost(String token, Long postId, PostRequestDTO.updatePost dto) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        PostDocument postDocument = postDocRepository.findById(post.getDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); // todo : NOT_FOUND_POST_CONTENT
        //권한 체크. 글 작성자만 수정 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
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
        //권한 체크. 글 작성자만 삭제 가능
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        commentRefService.deleteAllByPost(post);
        postRepository.delete(post);
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.INVALID_ARGUMENT) //todo : NOT_FOUND_POST
        );
    }

    /**
     * 임시저장글
     */
    public Post createTempPost(String token, PostRequestDTO.createTempPost dto) {
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
                .isTemp(true)
                .build();

        return postRepository.save(post);
    }
}