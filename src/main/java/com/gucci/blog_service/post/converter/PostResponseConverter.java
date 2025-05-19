package com.gucci.blog_service.post.converter;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostSearch;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public class PostResponseConverter {
    private PostResponseConverter() {
        throw new IllegalStateException("Util Class");
    }

    public static PostResponseDTO.GetPostDetail toGetPostDetailDto(Post post, String content, List<String> tagNameList) {
        return PostResponseDTO.GetPostDetail.builder()
                .postId(post.getPostId())
                .authorId(post.getUserId())
                .authorNickname("임시")
                .view(post.getView())
                .title(post.getTitle())
                .content(content)
                .tagNameList(tagNameList)
                .categoryCode(post.getCategory().getCategoryType().getCode())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public static PostResponseDTO.GetPost toGetPostDto(Post post, String firstImageUrl, List<String> tagNameList) {
        return PostResponseDTO.GetPost.builder()
                .postId(post.getPostId())
                .authorId(post.getUserId())
                .authorNickname(post.getUserNickName())
                .title(post.getTitle())
                .thumbnail(firstImageUrl)
                .view(post.getView())
                .categoryCode(post.getCategory().getCategoryId())
                .summary(post.getSummary())
                .tagNameList(tagNameList)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public static PostResponseDTO.GetPostList toGetPostList(Page<Post> postPage, List<PostResponseDTO.GetPost> posts) {
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


    /** pageImpl에서 변환*/
    public static PostResponseDTO.GetPost toGetPost(PostSearch postSearch) {
        return PostResponseDTO.GetPost.builder()
                .postId(Long.parseLong(postSearch.getPostId(), 16))
                .authorId(null) // PostSearch에 authorId 필드가 있으면 매핑, 없으면 null
                .authorNickname(postSearch.getAuthor())
                .title(postSearch.getTitle())
                .summary(null) // summary 필드가 있으면 매핑
                .thumbnail(null) // thumbnail 필드가 있으면 매핑
                .view(postSearch.getViewCount()) // 오타: viewCount로 변경 권장
                .tagNameList(postSearch.getTags())
                .categoryCode(null) // categoryCode 필드 있으면 매핑
                .createdAt(postSearch.getCreatedAt()) //.toLocalDateTime()) // OffsetDateTime → LocalDateTime 변환
                .updatedAt(null) // updatedAt 필드 있으면 매핑
                .build();
    }

    public static PostResponseDTO.GetPostList toGetPostList(Page<PostSearch> page) {
        List<PostResponseDTO.GetPost> postList = page.getContent().stream()
                .map(PostResponseConverter::toGetPost) // 위에서 만든 변환 메서드
                .toList();

        return PostResponseDTO.GetPostList.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .postList(postList)
                .build();
    }

    /**
     * 임시저장 글
     */
    public static PostResponseDTO.GetDraftDetail toGetDraftDetailDto(Post post, String content, List<String> tagNameList) {
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

    public static PostResponseDTO.GetDraft toGetDraftDto(Post post, String content, List<String> tagNameList) {
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
