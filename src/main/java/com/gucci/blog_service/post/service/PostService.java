package com.gucci.blog_service.post.service;

import com.gucci.blog_service.config.JwtTokenHelper;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
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


    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.INVALID_ARGUMENT) //todo : NOT_FOUND_POST
        );
    }
}