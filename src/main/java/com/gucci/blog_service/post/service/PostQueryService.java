package com.gucci.blog_service.post.service;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.repository.PostDocRepository;
import com.gucci.blog_service.post.repository.PostRepository;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostQueryService {
    private final PostRepository postRepository;
    private final PostDocRepository postDocRepository;

    public Post getPost(Long postId) {

        return postRepository.findById(postId).
                orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
    }

    public PostDocument getPostDocument(String postDocId) {
        return postDocRepository.findById(postDocId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST)); //todo : NOT_FOUND_POST_CONTENT
    }
}
