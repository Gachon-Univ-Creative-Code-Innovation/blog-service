package com.gucci.blog_service.comment.domain.dto;

import lombok.Getter;

public class CommentRequestDTO {

    @Getter
    public static class CreateComment{
        Long postId;
        Long parentCommentId;
        String content;
    }

    @Getter
    public static class UpdateComment{
        String content;
    }
}
