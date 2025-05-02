package com.gucci.blog_service.tag.repository;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByPost(Post post);

    void deleteByPostAndTagNameIn(Post post, List<String> tagNameList);
}
