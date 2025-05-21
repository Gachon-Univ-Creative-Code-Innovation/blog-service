package com.gucci.blog_service.post.repository;

import com.gucci.blog_service.post.domain.PostSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostSearchRepository extends ElasticsearchRepository<PostSearch, String> {

    // match 쿼리처럼 작동함
    List<PostSearch> findByTitleContainingOrContentContaining(String title, String content);

    List<PostSearch> findSimilarByTags(List<String> tags);
}
