package com.gucci.blog_service.post.controller;

import com.gucci.blog_service.post.domain.PostSearch;
import com.gucci.blog_service.post.service.PostSearchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog-service/posts/search")
public class PostSearchController {

    private final PostSearchService postSearchService;


    @Operation(summary = "게시글 검색", description = "게시글 본문과 제목에서 키워드를 검색합니다 ")
    @GetMapping
    public List<PostSearch> search(@RequestParam String keyword) {
        return postSearchService.search(keyword);
    }
}

