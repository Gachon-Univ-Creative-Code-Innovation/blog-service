package com.gucci.blog_service.post.domain.dto;

import com.gucci.blog_service.post.domain.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PostRequestDTO {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePost {
        @Schema(description = "임시저장 글의 부모 post가 있을경우 입력. 아니면 null 입력", example = "null")
        Long parentPostId;

        @Schema(description = "임시저장 글로 생성할 경우 draftPostId 를 입력. 아니면 null 입력", example = "null")
        Long draftPostId;

        @Schema(description = "글 제목은 필수입니다", example = "글 제목")
        @NotBlank(message = "제목은 필수입니다.")
        String title;

        @Schema(description = "글 내용은 필수입니다", example = "글 내용")
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;

        @Schema(description = "tag가 없을 시 빈 리스트를 입력합니다", example = "[\"tag1\", \"tag2\"]")
        List<String> tagNameList;

        @Schema(description = "category code를 입력합니다. null 전송 시 자동으로 기타 카테고리로 분류됩니다.", example = "1")
        Long categoryCode;

        @Schema(description = "postType을 입력합니다. POST, MATCHING", example = "POST")
        private PostType postType;


    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePost {
        @Schema(description = "글 제목은 필수입니다", example = "글 제목")
        @NotBlank(message = "제목은 필수입니다.")
        String title;

        @Schema(description = "글 내용은 필수입니다", example = "글 내용")
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;

        @Schema(description = "tag가 없을 시 빈 리스트를 입력합니다", example = "[\"tag1\", \"tag2\"]")
        List<String> tagNameList;

        @Schema(description = "category code를 입력합니다. null 전송 시 자동으로 기타 카테고리로 분류됩니다.", example = "1")
        Long categoryCode;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDraft {
        @Schema(description = "임시저장 글 수정 시 임시저장 글 아이디를 입력합니다. 이외의 경우 null을 입력합니다.", example = "null")
        Long draftPostId;

        @Schema(description = "발행된 글에 대한 임시저장일 경우 발행된 글 아이디를 입력합니다. 이외의 경우 null을 입력합니다.", example = "null")
        Long parentPostId;

        @Schema(description = "제목은 필수입니다", example = "임시저장 글 제목")
        @NotBlank(message = "제목은 필수입니다.")
        String title;

        @Schema(description = "글 내용은 필수입니다", example = "임시저장 글 내용")
        @NotBlank(message = "글 내용은 필수입니다.")
        String content;

        @Schema(description = "tag가 없을 시 빈 리스트를 입력합니다", example = "[\"tag1\", \"tag2\"]")
        List<String> tagNameList;

        @Schema(description = "category code를 입력합니다. null 전송 시 자동으로 기타 카테고리로 분류됩니다.", example = "1")
        Long categoryCode;

        @Schema(description = "postType을 입력합니다. POST, MATCHING", example = "POST")
        private PostType postType;
    }

}
