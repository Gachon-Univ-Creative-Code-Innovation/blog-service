package com.gucci.blog_service.post.repository;

import com.gucci.blog_service.category.domain.Category;
import com.gucci.blog_service.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserId(Long userId);
    Optional<Post> findByParentPostId(Long parentPostId);
    Page<Post> findAllByPostIdIn(List<Long> parentPostIds, Pageable page);
    Page<Post> findAllByCategoryAndIsDraft(Category category, Boolean isDraft, Pageable page);

    @Query("""
        select p
        from Post p
        where p.createdAt >= :sevenDaysAgo
        order by p.view desc
        """)
    Page<Post> findAllTrending(LocalDateTime sevenDaysAgo, Pageable page);

    Page<Post> findAllByIsDraft(Boolean isDraft, Pageable page);


    // 태그를 포함하는 글 조회
    @Query("""
        select distinct p
        from Tag t
        join t.post p
        where t.tagName in :tagName
        and p.isDraft = false
        and p.userId <> :userId
        """)
    List<Post> findByTagsContaining(String tagName, Long userId);

    // 최신 글 조회
    List<Post> findTop10ByOrderByCreatedAtDesc();
}