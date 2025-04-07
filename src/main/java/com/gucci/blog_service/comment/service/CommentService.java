package com.gucci.blog_service.comment.service;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.domain.dto.CommentResponseDTO;
import com.gucci.blog_service.comment.repository.CommentRepository;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.service.PostService;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.beans.Transient;
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
        List<Comment> comments = commentRepository.findAllByPost(post);

        return comments.stream().map(
                comment -> CommentResponseDTO.GetComments.builder()
                        .parentCommentId(comment.getParentComment().getCommentId())
                        .commentId(comment.getCommentId())
                        .authorNickname("임시")
                        .authorId(0L)
                        .createTime(comment.getCreatedAt())
                        .updateTime(comment.getCreatedAt())
                        .content(comment.getContent())
                        .build()
        ).toList();
    }

    @Transactional //JPA 영속성 컨텍스트라면 변경된 필드만 감지해서 업데이트 해준다
    public Comment updateComment(Long commentId, CommentRequestDTO.UpdateComment updateComment) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new CustomException(ErrorCode.INVALID_ARGUMENT) //commentId에 해당하는 댓글이 없음
        );

        comment.updateContent(updateComment.getContent());
        return comment;
    }
}
