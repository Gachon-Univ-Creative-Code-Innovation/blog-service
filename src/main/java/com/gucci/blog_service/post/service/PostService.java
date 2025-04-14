package com.gucci.blog_service.post.service;

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

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostDocRepository postDocRepository;

    private final JwtTokenHelper jwtTokenHelper;

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

    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.INVALID_ARGUMENT) //todo : NOT_FOUND_POST
        );
    }
}