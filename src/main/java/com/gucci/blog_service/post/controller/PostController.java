package com.gucci.blog_service.post.controller;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.service.PostService;
import com.gucci.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/following")
    public ApiResponse<PostResponseDTO.GetPostList> getFollowingPostList(
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "page") int page
    ){
        PostResponseDTO.GetPostList getPostList = postService.getFollowingPostList(token, page);
        return ApiResponse.success(getPostList);
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<PostResponseDTO.GetPostList> getPostListByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name = "page") int page
    ){
        PostResponseDTO.GetPostList getPostList = postService.getPostListByCategory(categoryId, page);
        return ApiResponse.success(getPostList);
    }

    @PatchMapping("/{postId}")
    public ApiResponse<String> updatePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId, // 발행된 postId, draft를 이용해 수정할 경의 draft의 paerentpostId
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
     */
    @PostMapping("/drafts")
    public ApiResponse<String> createDraft(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid PostRequestDTO.createDraft dto
    ){
        Post post = postService.createDraft(token, dto);
        return ApiResponse.success(post.getPostId() + " 글이 임시저장되었습니다");
    }

    @GetMapping("/drafts/{draftPostId}")
    public ApiResponse<PostResponseDTO.GetDraftDetail> getDraftDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long draftPostId
    ){
        PostResponseDTO.GetDraftDetail response = postService.getDraftDetail(token, draftPostId);
        return ApiResponse.success(response);
    }

    @GetMapping("/drafts")
    public ApiResponse<PostResponseDTO.GetDraftList> getDraftList(
            @RequestHeader("Authorization") String token
    ){
        PostResponseDTO.GetDraftList response = postService.getDraftList(token);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/drafts/{draftPostId}")
    public ApiResponse<String> deleteDraft(
            @RequestHeader("Authorization") String token,
            @PathVariable Long draftPostId
    ){
        postService.deleteDraft(token, draftPostId);
        return ApiResponse.success("임시저장 글 삭제 완료");
    }

}
