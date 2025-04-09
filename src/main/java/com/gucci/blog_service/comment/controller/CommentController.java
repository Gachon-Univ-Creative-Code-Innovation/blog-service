package com.gucci.blog_service.comment.controller;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.domain.dto.CommentResponseDTO;
import com.gucci.blog_service.comment.service.CommentService;
import com.gucci.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/blog/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("")
    public ApiResponse<String> createComment(@RequestBody CommentRequestDTO.CreateComment createComment) {
        Comment comment = commentService.createComment(createComment);
        return ApiResponse.success(comment.getCommentId()+" 댓글이 정상적으로 생성되었습니다");
    }

    @GetMapping("")
    public ApiResponse<List<CommentResponseDTO.GetComments>> getComments(@RequestParam(name = "postId") Long postId){
        List<CommentResponseDTO.GetComments> getComments = commentService.getCommentsByPostId(postId);
        return ApiResponse.success(getComments);
    }

    @PatchMapping("")
    public ApiResponse<String> modifyComments(
            @RequestParam(name="commentId") Long commentId,
            @RequestBody CommentRequestDTO.UpdateComment updateComment
    ) {
        Comment comment = commentService.updateComment(commentId, updateComment);
        return ApiResponse.success(comment.getCommentId() + " 댓글 정상적으로 업데이트를 완료했습니다");
    }

    @DeleteMapping("")
    public ApiResponse<String> deleteComments(@RequestParam(name="commentId") Long commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.success("일정 삭제 완료");
    }
}
