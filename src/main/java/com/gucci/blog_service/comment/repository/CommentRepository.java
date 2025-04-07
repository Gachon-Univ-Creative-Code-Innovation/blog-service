package com.gucci.blog_service.comment.repository;

import com.gucci.blog_service.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
