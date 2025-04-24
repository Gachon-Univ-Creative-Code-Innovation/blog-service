package com.gucci.blog_service.comment.domain;

import com.gucci.blog_service.config.common.BaseEntity;
import com.gucci.blog_service.post.domain.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity { //todo: user연결하기
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(columnDefinition="text", nullable=false)
    private String content;

    @Column(columnDefinition="boolean default false")
    private Boolean isDeleted = false;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment")
    private List<Comment> childComments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;



    public void updateContent(String content){
        this.content = content;
    }

    public void setDeleted(){
        this.isDeleted = true;
        this.content = "삭제된 댓글입니다.";
        this.userId = null;
    }
}