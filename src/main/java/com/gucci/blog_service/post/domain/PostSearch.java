package com.gucci.blog_service.post.domain;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "post")
public class PostSearch {
    @Id
    private String id;


    private String title;
    private String author;
    private List<String> tags;
    private String content;
    private LocalDateTime createdAt;
}
