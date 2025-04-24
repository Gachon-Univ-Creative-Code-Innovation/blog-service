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

    /**
     * 블로그 글
     */
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

    @PatchMapping("/{postId}")
    public ApiResponse<String> updatePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId,
            @RequestBody @Valid PostRequestDTO.updatePost dto
    ){
        Post post = postService.updatePost(token, postId, dto);
        return ApiResponse.success(post.getPostId() + " 글이 정상적으로 수정되었습니다.");
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<String> deletePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId
    ){
        postService.deletePost(token, postId);
        return ApiResponse.success("글이 정상적으로 삭제되었습니다.");
    }


    /**
     * 임시저장 글
     * 임시저장했다가 글을 불러와서 수정하고 발행하면 어케됨?
     */
    @PostMapping("/temp")
    public ApiResponse<String> createTempPost(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid PostRequestDTO.createTempPost dto
    ){
        Post post = postService.createTempPost(token, dto);
        return ApiResponse.success(post.getPostId() + " 글이 임시저장되었습니다");
    }

}
