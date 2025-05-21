package com.gucci.blog_service.comment.repository;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPost(Post post);
    List<Comment> findAllByUserId(Long userId);
}