package com.gucci.blog_service.post.service;

import com.gucci.blog_service.post.domain.PostSearch;
import com.gucci.blog_service.post.repository.PostSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostSearchService {
    private final PostSearchRepository postSearchRepository;

    public void createSamplePost() {
        PostSearch post = PostSearch.builder()
                .title("ElasticSearch 연동 성공!")
                .author("김현지")
                .tags(List.of("Elasticsearch", "Spring", "검색"))
                .content("이 글은 Elasticsearch 색인 생성 테스트입니다.")
                .createdAt(LocalDateTime.now())
                .build();

        postSearchRepository.save(post); // 🔥 색인!
    }

    public List<PostSearch> search(String keyword) {
        return postSearchRepository.findByTitleContainingOrContentContaining(keyword, keyword);
    }
}
