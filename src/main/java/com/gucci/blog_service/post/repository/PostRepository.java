package com.gucci.blog_service.post.repository;

import com.gucci.blog_service.category.domain.Category;
import com.gucci.blog_service.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserId(Long userId);
    Optional<Post> findByParentPostId(Long parentPostId);
    Page<Post> findAllByPostIdIn(List<Long> parentPostIds, Pageable page);
    Page<Post> findAllByCategoryAndDraft(Category category, Boolean isDraft, Pageable page);

    @Query("select p from Post p" +
            "    where p.view > 100" +
            "order by p.createdAt desc " +
            "limit 30")
    Page<Post> findAllTrending(Pageable page);
}