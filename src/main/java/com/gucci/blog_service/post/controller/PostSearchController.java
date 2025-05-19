package com.gucci.blog_service.post.controller;

import com.gucci.blog_service.post.domain.PostSearch;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.service.PostSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog-service/posts/search")
public class PostSearchController {

    private final PostSearchService postSearchService;


    @Operation(summary = "게시글 검색", description = "게시글 본문과 제목에서 키워드를 검색합니다 ")
    @GetMapping
    public PostResponseDTO.GetPostList postSearch(
            @Schema(description = "검색할 키워드를 입력합니다")@RequestParam String keyword,
            @Schema(description = "1 : 관련도순, 2 : 최신순, 3 : 인기순") @RequestParam Integer sortBy,
            @Schema(description = "조회할 페이지 번호. 0부터 시작합니다", example = "0") @RequestParam(name = "page") int page
    ) {
        return postSearchService.search(keyword, sortBy, page);
    }

}

