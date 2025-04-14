package com.gucci.blog_service.post.repository;

import com.gucci.blog_service.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PostRepository extends JpaRepository<Post, Long> {
}