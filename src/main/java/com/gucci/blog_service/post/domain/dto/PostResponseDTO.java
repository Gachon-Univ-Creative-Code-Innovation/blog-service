package com.gucci.blog_service.post.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class PostResponseDTO {

    @Builder
    @Getter
    public static class GetPostDetail{
        Long postId;
        Long authorId;
        String authorNickname;
        String title;
        String content;
        Long view;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
        //todo : tag, category
    }

    @Builder
    @Getter
    public static class GetDraftDetail{
        Long postId;
        Long authorId;
        String authorNickname;
        String title;
        String content;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
        //todo : tag, category
    }
}
