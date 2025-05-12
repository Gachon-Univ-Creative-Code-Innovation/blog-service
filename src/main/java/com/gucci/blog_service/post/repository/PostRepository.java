package com.gucci.blog_service.post.repository;

import com.gucci.blog_service.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserId(Long userId);
    Optional<Post> findByParentPostId(Long parentPostId);
    Page<Post> findAllByPostIdIn(List<Long> parentPostIds, PageRequest pageRequest);
}