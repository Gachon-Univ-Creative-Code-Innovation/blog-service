package com.gucci.blog_service.post.repository;

import com.gucci.blog_service.category.domain.Category;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.enums.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserId(Long userId);
    Optional<Post> findByParentPostId(Long parentPostId);
    Page<Post> findAllByPostTypeAndPostIdIn(PostType postType, List<Long> parentPostIds, Pageable page);
    Page<Post> findAllByCategoryAndIsDraftAndPostType(Category category, Boolean isDraft, PostType postType, Pageable page);

    @Query("""
        select p
        from Post p
        where p.createdAt >= :sevenDaysAgo
                and p.postType in :postType
                and p.isDraft = false
        order by p.view desc
        """)
    Page<Post> findAllTrending(LocalDateTime sevenDaysAgo, PostType postType, Pageable page);

    Page<Post> findAllByIsDraftAndPostType(Boolean isDraft, PostType postType, Pageable page);

    Page<Post> findAllByPostType(PostType postType, Pageable page);
    // 태그를 포함하는 글 조회
    @Query("""
        select distinct p
        from Tag t
        join t.post p
        where t.tagName in :tagName
        and p.isDraft = false
        and p.postType in :postType
        and p.userId <> :userId
        """)
    List<Post> findByTagsContaining(String tagName, PostType postType, Long userId);

    // 최신 글 조회
    List<Post> findTop10ByOrderByCreatedAtDesc();
}