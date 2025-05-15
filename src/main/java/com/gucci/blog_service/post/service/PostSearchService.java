package com.gucci.blog_service.post.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.domain.PostSearch;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.repository.PostSearchRepository;
import com.gucci.blog_service.tag.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostSearchService {
    private final PostSearchRepository postSearchRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private final Logger logger = LoggerFactory.getLogger(PostSearchService.class);

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
                .veiwCount(post.getView())
                .build();

        postSearchRepository.save(postSearch);
    }

    /** 게시글 업데이트 시 반영 */
    public void update(Post post, PostDocument postDocument, List<String> tags) {
        //같은 id를 가지면 업데이트 됨
        index(post, postDocument, tags);
    }

    /** 조회수 반영 */
    public void updateViewCount(Long postId, Long viewCount) {
        try {
            UpdateRequest<Map<String, Object>, Map<String, Object>> request = UpdateRequest.of(u -> u
                    .index("posts")
                    .id(Long.toHexString(postId))
                    .doc(Map.of("viewCount", viewCount))
            );
            elasticsearchClient.update(request, Map.class);

        }catch (IOException e) {
            logger.error("Elasticsearch 조회수 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }

    }


    public List<PostSearch> search(String keyword) {
        return postSearchRepository.findByTitleContainingOrContentContaining(keyword, keyword);
    }

    /** 게시글 삭제 */
    public void delete(Long postId) {
        postSearchRepository.deleteById(Long.toHexString(postId));
    }
}
