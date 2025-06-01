package com.gucci.blog_service.post.controller;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.service.PostService;
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
@RequestMapping("/api/blog-service/posts")
public class PostController {
    private final PostService postService;

    /**
     * 블로그 글
     */
    @Operation(summary = "게시글 생성", description = "게시글을 생성합니다. 게시글 타입은 POST, MATCHING 입니다. 임시저장 글로 생성할 경우 draftPostId 를 입력. 아니면 null. 임시저장글의 부모 post가 있을경우 parentPostId 입력. 아니면 null")
    @PostMapping("")
    public ApiResponse<String> createPost(
            HttpServletRequest request,
            @RequestBody @Valid PostRequestDTO.CreatePost post
    ){
        String token = request.getHeader("Authorization");
        Post newPost = postService.createPost(token, post);
        return ApiResponse.success(newPost.getPostId() + " 글이 정상적으로 생성되었습니다.");
    }

    /** 페이징 적용 안함 */
    @Operation(summary = "내 글 조회", description = "본인이 작성한 글을 조회합니다.")
    @GetMapping()
    public ApiResponse<List<PostResponseDTO.GetPost>> getMyPosts(
            HttpServletRequest request
    ){
        String token = request.getHeader("Authorization");
        List<PostResponseDTO.GetPost> getPostList = postService.getMyPostList(token);
        return ApiResponse.success(getPostList);
    }

    @Operation(summary = "사용자 글 조회", description = "다른 사용자가 작성한 글을 조회합니다")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<PostResponseDTO.GetPost>> getUserPosts(
            @Schema(description = "조회할 사용자의 userId", example = "0") @PathVariable Long userId
    ) {
        List<PostResponseDTO.GetPost> getPostList = postService.getMyPostList(userId);
        return ApiResponse.success(getPostList);
    }

    @Operation(summary = "게시글 한 개 조회", description = "게시글 한 개를 상세 조회합니다.")
    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDTO.GetPostDetail> getPostDetail(
            @Schema(description = "조회할 글의 postId", example = "0") @PathVariable Long postId
    ){
        PostResponseDTO.GetPostDetail getPostDetail = postService.getPostDetail(postId);
        return ApiResponse.success(getPostDetail);
    }

    @Operation(summary = "팔로잉 글 조회", description = "사용자가 팔로잉하고 있는 사용자들의 글 리스트를 조회합니다. 최신순으로 정렬됩니다.")
    @GetMapping("/following")
    public ApiResponse<PostResponseDTO.GetPostList> getFollowingPostList(
            HttpServletRequest request,
            @Schema(description = "조회할 페이지 번호. 0부터 시작합니다.", example = "0") @RequestParam(name = "page") int page
    ){
        String token = request.getHeader("Authorization");
        PostResponseDTO.GetPostList getPostList = postService.getFollowingPostList(token, page);
        return ApiResponse.success(getPostList);
    }

    @Operation(summary = "전체 글 조회", description = "전체 글 목록을 조회합니다")
    @GetMapping("/all")
    public ApiResponse<PostResponseDTO.GetPostList> getAllPostList(
            @Schema(description = "조회할 글의 종류 POST, MATCHING") @RequestParam String postType,
            @Schema(description = "조회할 페이지 번호. 0부터 시작합니다", example = "0") @RequestParam(name = "page") int page
    ){
        PostResponseDTO.GetPostList getPostList = postService.getPostAll(postType, page);
        return ApiResponse.success(getPostList);
    }

    @Operation(summary = "카테고리 별 글 조회", description = "카테고리 별 글 리스트를 조회합니다. 최신순으로 정렬됩니다.")
    @GetMapping("/category/{categoryId}")
    public ApiResponse<PostResponseDTO.GetPostList> getPostListByCategory(
            @Schema(description = "조회할 카테고리 id를 입력합니다.", example = "0") @PathVariable Long categoryId,
            @Schema(description = "조회할 페이지 번호. 0부터 시작합니다", example = "0") @RequestParam(name = "page") int page
    ){
        PostResponseDTO.GetPostList getPostList = postService.getPostListByCategory(categoryId, page);
        return ApiResponse.success(getPostList);
    }

    @Operation(summary = "인기글 조회", description = "인기글 리스트를 조회합니다. 최신순으로 정렬됩니다. 조회수를 기준으로 선정됩니다.")
    @GetMapping("/trending")
    public ApiResponse<PostResponseDTO.GetPostList> getTrendingPostList(
            @Schema(description = "조회할 페이지 번호. 0부터 시작합니다", example = "0") @RequestParam(name = "page") int page
    ){
        PostResponseDTO.GetPostList getPostList = postService.getTrendingPostList(page);
        return ApiResponse.success(getPostList);
    }

    @Operation(summary = "추천글 조회", description = "사용자의 대표 태그를 기준으로 추천글을 조회합니다. 관련도순으로 정렬됩니다")
    @GetMapping("/recommend")
    public ApiResponse<PostResponseDTO.GetPostList> getRecommendPostList(
            HttpServletRequest request,
            @Schema(description = "조회할 페이지 번호. 0부터 시작합니다", example = "0") @RequestParam(name = "page") int page
    ){
        String token = request.getHeader("Authorization");
        PostResponseDTO.GetPostList getPostList = postService.getRecommendPostList(token, page);
        return ApiResponse.success(getPostList);
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다. 작성한 본인만 수정가능합니다.")
    @PatchMapping("/{postId}")
    public ApiResponse<String> updatePost(
            HttpServletRequest request,
            @Schema(description = "수정할 글의 postId를 입력합니다. 임시저장글을 이용해 수정할 경우 paerentPostId를 입력합니다")
            @PathVariable Long postId,
            @RequestBody @Valid PostRequestDTO.UpdatePost dto
    ){
        String token = request.getHeader("Authorization");
        Post post = postService.updatePost(token, postId, dto);
        return ApiResponse.success(post.getPostId() + " 글이 정상적으로 수정되었습니다.");
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. 작성한 본인만 삭제가능합니다.")
    @DeleteMapping("/{postId}")
    public ApiResponse<String> deletePost(
            HttpServletRequest request,
            @Schema(description = "삭제할 글의 postId를 입력합니다")
            @PathVariable Long postId
    ){
        String token = request.getHeader("Authorization");
        postService.deletePost(token, postId);
        return ApiResponse.success("글이 정상적으로 삭제되었습니다.");
    }


    /**
     * 임시저장 글
     */
    @Operation(summary = "임시저장", description = "draftPostId : 임시저장 글 수정 시 임시저장 글 아이디를 입력, 이외 null.   " +
            "parentPostId : 발행된 글에 대한 임시저장일 경우 발행된 글 아이디를 입력, 이외 null")
    @PostMapping("/drafts")
    public ApiResponse<String> createDraft(
            HttpServletRequest request,
            @RequestBody @Valid PostRequestDTO.CreateDraft dto
    ){
        String token = request.getHeader("Authorization");
        Post post = postService.createDraft(token, dto);
        return ApiResponse.success(post.getPostId() + " 글이 임시저장되었습니다");
    }

    @Operation(summary = "임시저장 상세조회", description = "임시저장 글을 상세조회합니다")
    @GetMapping("/drafts/{draftPostId}")
    public ApiResponse<PostResponseDTO.GetDraftDetail> getDraftDetail(
            HttpServletRequest request,
            @Schema(description = "임시저장글 id") @PathVariable Long draftPostId
    ){
        String token = request.getHeader("Authorization");
        PostResponseDTO.GetDraftDetail response = postService.getDraftDetail(token, draftPostId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "임시저장 목록 조회", description = "임시저장 목록을 조회합니다.")
    @GetMapping("/drafts")
    public ApiResponse<PostResponseDTO.GetDraftList> getDraftList(
            HttpServletRequest request
    ){
        String token = request.getHeader("Authorization");
        PostResponseDTO.GetDraftList response = postService.getDraftList(token);
        return ApiResponse.success(response);
    }

    @Operation(summary = "임시저장 삭제", description = "임시저장 글을 삭제합니다")
    @DeleteMapping("/drafts/{draftPostId}")
    public ApiResponse<String> deleteDraft(
            HttpServletRequest request,
            @Schema(description = "삭제할 임시저장 글 id")@PathVariable Long draftPostId
    ){
        String token = request.getHeader("Authorization");
        postService.deleteDraft(token, draftPostId);
        return ApiResponse.success("임시저장 글 삭제 완료");
    }

}
