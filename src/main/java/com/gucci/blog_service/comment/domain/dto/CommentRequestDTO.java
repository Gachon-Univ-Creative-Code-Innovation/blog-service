package com.gucci.blog_service.comment.domain.dto;

import lombok.Getter;
import lombok.Setter;

public class CommentRequestDTO {

    @Getter
    @Setter
    public static class CreateComment{
        Long postId;
        Long parentCommentId;
        String content;
    }

    @Getter
    @Setter
    public static class UpdateComment{
        String content;
    }
}
