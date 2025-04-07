package com.gucci.blog_service.post.service;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }
}
