package com.gucci.blog_service.post.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class PostRequestDTO {
    @Getter
    @Builder
    public static class createPost{
        Long postId; //임시저장 글로 생성할 경우 draftpostId. 아니면 null
        @NotBlank(message = "제목은 필수입니다.")
        String title;
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;
        List<String> tagNameList;
        Long categoryCode;
    }

    @Getter
    @Builder
    public static class updatePost{
        @NotBlank(message = "제목은 필수입니다.")
        String title;
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;
        List<String> tagNameList;
        Long categoryCode;
        //todo : category
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
        List<String> tagNameList;
        //todo : category
    }

}
