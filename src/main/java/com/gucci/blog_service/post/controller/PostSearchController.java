package com.gucci.blog_service.post.controller;

import com.gucci.blog_service.post.domain.PostSearch;
import com.gucci.blog_service.post.service.PostSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class PostSearchController {

    private final PostSearchService postSearchService;

    public PostSearchController(PostSearchService postSearchService) {
        this.postSearchService = postSearchService;
    }

    @PostMapping("/init")
    public String initData() {
        postSearchService.createSamplePost();
        return "색인 완료!";
    }

    @GetMapping
    public List<PostSearch> search(@RequestParam String q) {
        return postSearchService.search(q);
    }
}

