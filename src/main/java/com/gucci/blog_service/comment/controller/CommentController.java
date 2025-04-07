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
@RequestMapping("api/blog/")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("comments")
    public ApiResponse<String> createComment(@RequestBody CommentRequestDTO.CreateComment createComment) {
        Comment comment = commentService.createComment(createComment);
        return ApiResponse.success(comment.getCommentId()+" 댓글이 정상적으로 생성되었습니다");
    }

    @GetMapping("comments/")
    public ApiResponse<List<CommentResponseDTO.GetComments>> getComments(@RequestParam(name = "postId") Long postId){
        List<CommentResponseDTO.GetComments> getComments = commentService.getCommentsByPostId(postId);
        return ApiResponse.success(getComments);
    }


}
