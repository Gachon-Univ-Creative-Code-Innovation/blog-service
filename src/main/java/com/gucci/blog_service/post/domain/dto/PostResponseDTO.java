package com.gucci.blog_service.post.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
        String summary;
        String thumbnail;
        Long view;
        List<String> tagNameList;
        Long categoryCode;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    public static class GetPostList{
        @Schema(description = "페이징 적용 전 전체 element개수")
        Long totalElements;
        @Schema(description = "전체 페이지 개수")
        Integer totalPages;
        @Schema(description = "현재 페이지 번호")
        Integer pageNumber;
        @Schema(description = "패이지당 element개수")
        Integer pageSize;
        @Schema(description = "첫번째 페이지이면 true를 반환")
        Boolean isFirst;
        @Schema(description = "마지막 페이지이면 true를 반환")
        Boolean isLast;
        @Schema(description = "게시글 리스트")
        List<GetPost> postList;
    }


    @Builder
    @Getter
    public static class GetDraftDetail{
        Long draftPostId;
        @Schema(description = "임시저장글의 부모 글(게시된 글)")
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
