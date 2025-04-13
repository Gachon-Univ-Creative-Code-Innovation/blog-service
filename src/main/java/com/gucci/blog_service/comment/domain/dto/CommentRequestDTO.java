package com.gucci.blog_service.comment.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class CommentRequestDTO {

    @Getter
    @Setter
    public static class CreateComment{
        @NotBlank(message = "postId는 필수입니다.")
        Long postId;
        Long parentCommentId;
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content;
    }

    @Getter
    @Setter
    public static class UpdateComment{
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content;
    }
}
