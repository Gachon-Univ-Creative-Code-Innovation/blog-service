package com.gucci.blog_service.post.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "post")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDocument {
    @Id
    private String id;

    private String content;

    public void updateContent(String content) {
        this.content = content;
    }
}
