package com.gucci.blog_service.comment.service;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.domain.dto.CommentResponseDTO;
import com.gucci.blog_service.comment.repository.CommentRepository;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<CommentResponseDTO.GetComments> getCommentsByPostId(Long postId) {
        Post post = postService.getPostById(postId);
        List<Comment> comments = commentRepository.findAllByPost(post).orElse(null);

        assert comments != null;

        return comments.stream().map(
                comment -> CommentResponseDTO.GetComments.builder()
                        .parentCommentId(comment.getParentComment().getCommentId())
                        .commentId(comment.getCommentId())
                        .authorNickname("임시")
                        .authorId(0L)
                        .createTime()
                        .updateTime()
                        .content(comment.getContent())
                        .build()
        ).toList();
    }
}
