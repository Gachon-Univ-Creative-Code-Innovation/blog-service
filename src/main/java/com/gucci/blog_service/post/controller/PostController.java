package com.gucci.blog_service.post.controller;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.service.PostService;
import com.gucci.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog-service/posts")
public class PostController {
    private final PostService postService;

    @PostMapping("")
    public ApiResponse<String> createPost(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid PostRequestDTO.createPost post
    ){
        Post newPost = postService.createPost(token, post);
        return ApiResponse.success(newPost.getPostId() + " 글이 정상적으로 생성되었습니다.");
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDTO.GetPostDetail> getPostDetail(
            @PathVariable Long postId
    ){
        PostResponseDTO.GetPostDetail getPostDetail = postService.getPostDetail(postId);
        return ApiResponse.success(getPostDetail);
    }
}
