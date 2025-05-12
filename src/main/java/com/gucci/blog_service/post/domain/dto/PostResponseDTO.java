package com.gucci.blog_service.post.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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
        List<String> tagNameList;
        Long categoryCode;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    public static class GetPost{
        Long postId;
        Long authorId;
        String authorNickname;
        String title;
        String content;
        Long view;
        List<String> tagNameList;
        Long categoryCode;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    public static class GetPostList{
        Long totalElements;
        Integer totalPages;
        Integer pageNumber;
        Integer pageSize;
        Boolean isFirst;
        Boolean isLast;
        List<GetPost> postList;
    }


    @Builder
    @Getter
    public static class GetDraftDetail{
        Long draftPostId;
        Long parentPostId;
        Long authorId;
        String authorNickname;
        String title;
        String content;
        List<String> tagNameList;
        Long categoryCode;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    public static class GetDraft{
        Long draftPostId;
        String title;
        String content;
        List<String> tagNameList;
        Long categoryCode;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    public static class GetDraftList{
        List<GetDraft> draftList;
    }
}
