package com.gucci.blog_service.comment.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Comments;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponseDTO {

    @Getter
    @Builder
    public static class GetComment{
        Long commentId;
        Long parentCommentId;
        Long authorId;
        String authorNickname;
        String content;
        Integer depth;
        Boolean isDeleted;
        LocalDateTime createTime;
        LocalDateTime updateTime;
    }

    @Getter
    @Builder
    public static class GetCommentList{
        List<GetComment> commentList;
    }

}
