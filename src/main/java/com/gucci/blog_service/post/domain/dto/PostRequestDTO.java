package com.gucci.blog_service.post.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

public class PostRequestDTO {
    @Getter
    @Builder
    public static class createPost{
        Long postId;
        @NotBlank(message = "제목은 필수입니다.")
        String title;
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;
        //todo : tag, category
    }

    @Getter
    @Builder
    public static class updatePost{
        @NotNull(message = "post id 값은 필수입니다.")
        Long postId; // 발행된 postId, draft를 이용해 수정할 경의 draft의 paerentpostId
        @NotBlank(message = "제목은 필수입니다.")
        String title;
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;
        //todo : tag. category
    }

    @Getter
    @Builder
    public static class createDraft{
        Long draftPostId; //임시저장 글 아이디
        Long parentPostId; //발행된 글 아이디
        @NotBlank(message = "제목은 필수입니다.")
        String title;
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;
        //todo : tag. category
    }
}
