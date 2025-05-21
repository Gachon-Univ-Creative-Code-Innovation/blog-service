package com.gucci.blog_service.post.converter;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public class PostResponseConverter {
    private PostResponseConverter() {
        throw new IllegalStateException("Util Class");
    }

    public static PostResponseDTO.GetPostDetail toGetPostDetailDto(Post post, String content, Set<String> tagNameList) {
        return PostResponseDTO.GetPostDetail.builder()
                .postId(post.getPostId())
                .authorId(post.getUserId())
                .authorNickname(post.getUserNickName())
                .view(post.getView())
                .title(post.getTitle())
                .content(content)
                .tagNameList(tagNameList)
                .categoryCode(post.getCategory().getCategoryType().getCode())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public static PostResponseDTO.GetPost toGetPostDto(Post post, String thumbnail, Set<String> tagNameList) {
        return PostResponseDTO.GetPost.builder()
                .postId(post.getPostId())
                .authorId(post.getUserId())
                .authorNickname(post.getUserNickName())
                .title(post.getTitle())
                .thumbnail(thumbnail)
                .view(post.getView())
                .categoryCode(post.getCategory().getCategoryId())
                .summary(post.getSummary())
                .tagNameList(tagNameList)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public static PostResponseDTO.GetPostList toGetPostList(Page<?> postPage, List<PostResponseDTO.GetPost> posts) {
        return PostResponseDTO.GetPostList.builder()
                .pageSize(postPage.getSize()) //페이지당 element개수
                .pageNumber(postPage.getNumber()) //현재 페이지 번호
                .totalElements(postPage.getTotalElements()) //페이징 적용 전 전체 element개수
                .totalPages(postPage.getTotalPages()) //전체 페이지 개수
                .isFirst(postPage.isFirst())
                .isLast(postPage.isLast())
                .postList(posts)
                .build();
    }
    

    /**
     * 임시저장 글
     */
    public static PostResponseDTO.GetDraftDetail toGetDraftDetailDto(Post post, String content, Set<String> tagNameList) {
        return PostResponseDTO.GetDraftDetail.builder()
                .draftPostId(post.getPostId())
                .parentPostId(post.getParentPostId())
                .authorId(post.getUserId())
                .title(post.getTitle())
                .authorNickname(post.getUserNickName())
                .content(content)
                .tagNameList(tagNameList)
                .categoryCode(post.getCategory().getCategoryType().getCode())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public static PostResponseDTO.GetDraft toGetDraftDto(Post post, String content, Set<String> tagNameList) {
        return PostResponseDTO.GetDraft.builder()
                .draftPostId(post.getPostId())
                .title(post.getTitle())
                .content(content)
                .tagNameList(tagNameList)
                .categoryCode(post.getCategory().getCategoryType().getCode())
                .updatedAt(post.getUpdatedAt())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
