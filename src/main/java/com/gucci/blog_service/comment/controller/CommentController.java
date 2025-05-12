package com.gucci.blog_service.comment.controller;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.domain.dto.CommentResponseDTO;
import com.gucci.blog_service.comment.service.CommentService;
import com.gucci.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/blog-service/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("")
    public ApiResponse<String> createComment(
            HttpServletRequest request,
            @RequestBody @Valid CommentRequestDTO.CreateComment createComment
    ) {
        String token = request.getHeader("Authorization");
        Comment comment = commentService.createComment(createComment, token);
        return ApiResponse.success(comment.getCommentId()+" 댓글이 정상적으로 생성되었습니다");
    }

    @GetMapping("/{postId}")
    public ApiResponse<CommentResponseDTO.GetCommentList> getComments(
            @PathVariable Long postId){
        CommentResponseDTO.GetCommentList getComments = commentService.getCommentsByPostId(postId);
        return ApiResponse.success(getComments);
    }

    @PatchMapping("/{commentId}")
    public ApiResponse<String> modifyComments(
            HttpServletRequest request,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequestDTO.UpdateComment updateComment
    ) {
        String token = request.getHeader("Authorization");
        Comment comment = commentService.updateComment(commentId, updateComment, token);
        return ApiResponse.success(comment.getCommentId() + " 댓글 정상적으로 업데이트를 완료했습니다");
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<String> deleteComments(
            HttpServletRequest request,
            @PathVariable Long commentId
    ) {
        String token = request.getHeader("Authorization");
        commentService.deleteComment(commentId, token);
        return ApiResponse.success("댓글 삭제 완료");
    }
}
