package com.gucci.blog_service.comment.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CommentRequestDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateComment{
        @Schema(description = "postId는 필수입니다.")
        @NotNull(message = "postId는 필수입니다.")
        Long postId;

        @Schema(description = "root댓글일 경우 null을 입력합니다. 대댓글일 경우 부모 댓글 id를 입력합니다. ")
        Long parentCommentId;

        @Schema(description = "댓글 내용은 필수입니다.", example = "댓글 내용")
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateComment{
        @Schema(description = "댓글 내용은 필수입니다.", example = "수정 댓글 내용")
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content;
    }
}
