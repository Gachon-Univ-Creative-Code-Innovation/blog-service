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

import java.util.List;

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

    @PatchMapping("")
    public ApiResponse<String> updatePost(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid PostRequestDTO.updatePost dto
    ){
        Post post = postService.updatePost(token, dto);
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
     */
    @PostMapping("/draft")
    public ApiResponse<String> createDraft(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid PostRequestDTO.createDraft dto
    ){
        Post post = postService.createDraft(token, dto);
        return ApiResponse.success(post.getPostId() + " 글이 임시저장되었습니다");
    }

    @GetMapping("/draft/{postId}")
    public ApiResponse<PostResponseDTO.GetDraftDetail> getDraftDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId
    ){
        PostResponseDTO.GetDraftDetail response = postService.getDraftDetail(token, postId);
        return ApiResponse.success(response);
    }

    @GetMapping("/draft")
    public ApiResponse<PostResponseDTO.GetDraftList> getDraft(
            @RequestHeader("Authorization") String token
    ){
        PostResponseDTO.GetDraftList response = postService.getDraftList(token);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/draft/{postId}")
    public ApiResponse<String> deleteDraft(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId
    ){
        postService.deleteDraft(token, postId);
        return ApiResponse.success("임시저장 글 삭제 완료");
    }

}
