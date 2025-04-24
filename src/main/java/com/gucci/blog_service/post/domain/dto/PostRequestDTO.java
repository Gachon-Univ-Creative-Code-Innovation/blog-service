package com.gucci.blog_service.post.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class PostRequestDTO {
    @Getter
    public static class createPost{
        @NotBlank(message = "제목은 필수입니다.")
        String title;
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;
        //todo : tag, category
    }

    @Getter
    public static class updatePost{
        @NotBlank(message = "제목은 필수입니다.")
        String title;
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;
        //todo : tag. category
    }

    @Getter
    public static class createTempPost{
        @NotBlank(message = "제목은 필수입니다.")
        String title;
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;
        //todo : tag. category
    }
}
