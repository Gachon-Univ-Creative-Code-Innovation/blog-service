package com.gucci.blog_service.post.domain;

import com.gucci.blog_service.config.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity { //todo: user연결하기
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long postId;

    private Long parentPostId; //임시저장된 글의 원본 글 (발행되어있는)

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    private String documentId;

    @Column(columnDefinition = "bigint default 0", nullable = false)
    private Long view = 0L;

    private String content;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean isDraft = false;

    public void updateTitle(String title) {
        this.title = title;
    }
    public void publish() {
        this.isDraft = false;
    }
}