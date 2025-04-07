package com.gucci.blog_service.post.domain;

import com.gucci.blog_service.config.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity { //todo: user연결하기
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long postId;

    @Column(nullable = false)
    private String title;

    private Long documentId;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Long view;
}
