package com.gucci.blog_service.comment.service;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.repository.CommentRepository;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PostService postService;
    private final CommentRepository commentRepository;

    public Comment createComment(CommentRequestDTO.CreateComment createComment) {
        Post post = postService.getPostById(createComment.getPostId());
        Comment parentComment = commentRepository.findById(createComment.getParentCommentId()).orElse(null);

        Comment newComment = Comment.builder()
                .post(post)
                .content(createComment.getContent())
                .parentComment(parentComment)
                .build();

        return commentRepository.save(newComment);
    }
}
