package com.gucci.blog_service.post.domain;

import com.gucci.blog_service.category.domain.Category;
import com.gucci.blog_service.config.common.BaseEntity;
import com.gucci.blog_service.post.domain.enums.PostType;
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
@Table(name = "post", indexes = {
        @Index(name = "idx_created_at", columnList = "createdAt")
})
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

    private String thumbnail; //thumbnail 이미지의 objectkey만 저장

    @Column(columnDefinition = "bigint default 0", nullable = false)
    private Long view = 0L;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean isDraft = false;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Category category;

//    // 양방향 관계 설정
//    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Tag> tags = new ArrayList<>();

    public void updateView() {
        this.view = this.view + 1;
    }

    public void update(String title, String summary, Category category, String thumbnail) {
        this.title = title;
        this.summary = summary;
        this.category = category;
        this.thumbnail = thumbnail;
    }
    public void update(String title, Category category, String thumbnail) {
        this.title = title;
        this.category = category;
        this.thumbnail = thumbnail;
    }
    public void update(String title, Category category) {
        this.title = title;
        this.category = category;
    }

    public void publish() {
        this.isDraft = false;
    }
}