package com.gucci.blog_service.post.service;

import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.domain.PostSearch;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.repository.PostSearchRepository;
import com.gucci.blog_service.tag.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostSearchService {
    private final PostSearchRepository postSearchRepository;

    /** mongodb post -> elasticsearch에 인덱싱 */
    public void index(Post post, PostDocument postDocument, List<String> tags) {
        //LocalDateTime -> OffsetDateTime 변환
        OffsetDateTime odt = post.getCreatedAt().atOffset(ZoneOffset.UTC);

        PostSearch postSearch = PostSearch.builder()
                .postId(Long.toHexString(post.getPostId()))
                .title(post.getTitle())
                .author(post.getUserNickName())
                .tags(tags)
                .content(postDocument.getContent())
                .createdAt(odt)
                .build();

        postSearchRepository.save(postSearch);
    }

    public void update(Post post, PostDocument postDocument, List<String> tags) {
        //같은 id를 가지면 업데이트 됨
        index(post, postDocument, tags);
    }

    public List<PostSearch> search(String keyword) {
        return postSearchRepository.findByTitleContainingOrContentContaining(keyword, keyword);
    }

    /** 게시글 삭제 */
    public void delete(Long postId) {
        postSearchRepository.deleteById(Long.toHexString(postId));
    }
}
