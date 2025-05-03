package com.gucci.blog_service.post.domain;

import com.gucci.blog_service.category.domain.Category;
import com.gucci.blog_service.config.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    private String summary;

    @Column(columnDefinition = "bigint default 0", nullable = false)
    private Long view = 0L;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean isDraft = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Category category;

    public void updateTitle(String title) {
        this.title = title;
    }
    public void updateCategory(Category category) {
        this.category = category;
    }
    public void publish(String title) {
        this.title = title;
        this.isDraft = false;
    }
}