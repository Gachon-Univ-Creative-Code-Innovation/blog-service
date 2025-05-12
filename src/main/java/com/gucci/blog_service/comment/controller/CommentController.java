package com.gucci.blog_service.comment.controller;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.domain.dto.CommentResponseDTO;
import com.gucci.blog_service.comment.service.CommentService;
import com.gucci.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(summary = "댓글 생성", description = "댓글을 생성합니다")
    @PostMapping("")
    public ApiResponse<String> createComment(
            HttpServletRequest request,
            @RequestBody @Valid CommentRequestDTO.CreateComment createComment
    ) {
        String token = request.getHeader("Authorization");
        Comment comment = commentService.createComment(createComment, token);
        return ApiResponse.success(comment.getCommentId()+" 댓글이 정상적으로 생성되었습니다");
    }

    @Operation(summary = "게시글의 댓글 조회", description = "게시글에 해당하는 댓글을 조회합니다")
    @GetMapping("/{postId}")
    public ApiResponse<CommentResponseDTO.GetCommentList> getComments(
            @Schema(description = "댓글을 조회하고 싶은 게시글 id") @PathVariable Long postId){
        CommentResponseDTO.GetCommentList getComments = commentService.getCommentsByPostId(postId);
        return ApiResponse.success(getComments);
    }

    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다. 댓글 작성자 본인만 수정가능합니다.")
    @PatchMapping("/{commentId}")
    public ApiResponse<String> modifyComments(
            HttpServletRequest request,
            @Schema(description = "수정할 댓글 id") @PathVariable Long commentId,
            @RequestBody @Valid CommentRequestDTO.UpdateComment updateComment
    ) {
        String token = request.getHeader("Authorization");
        Comment comment = commentService.updateComment(commentId, updateComment, token);
        return ApiResponse.success(comment.getCommentId() + " 댓글 정상적으로 업데이트를 완료했습니다");
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 댓글 작성자나 게시글 작성자만 삭제가능합니다.")
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
