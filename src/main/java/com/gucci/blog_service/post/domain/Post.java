package com.gucci.blog_service.post.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Post { //todo: user연결하기
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
