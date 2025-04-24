package com.gucci.blog_service.comment.repository;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPost(Post post);
}
