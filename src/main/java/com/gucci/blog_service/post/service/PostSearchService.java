package com.gucci.blog_service.post.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.gucci.blog_service.post.converter.PostResponseConverter;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.domain.PostSearch;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.repository.PostSearchRepository;
import com.gucci.blog_service.tag.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        //OffsetDateTime odt = post.getCreatedAt().atOffset(ZoneOffset.UTC);

        PostSearch postSearch = PostSearch.builder()
                .postId(Long.toHexString(post.getPostId()))
                .title(post.getTitle())
                .author(post.getUserNickName())
                .tags(tags)
                .content(postDocument.getContent())
                .createdAt(post.getCreatedAt())
                .viewCount(post.getView())
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


    public PostResponseDTO.GetPostList search(String keyword, Integer sortBy, Integer page) {
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        try{
            Query query = buildSearchQuery(keyword);
            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index("post")                 // 2. 검색 대상 Elasticsearch 인덱스명을 지정 ("post" 인덱스)
                    .query(query)                        // 3. 실제 검색 쿼리(Query)를 설정
                    .from(page * size)             // 4. 페이징: 현재 페이지에 해당하는 첫 번째 결과 위치 지정
                    .size(size);                         // 5. 한 페이지에 가져올 검색 결과 개수

            switch (sortBy) {
                case 1: // 관련도 순 정렬
                    break;
                case 2: // 최신순 정렬
                    builder.sort(s -> s.field(f -> f.
                            field("createdAt")
                            .order(SortOrder.Desc)
                    ));
                    break;
                case 3: // 인기순(조회수기준) 정렬
                    builder.sort(s -> s.field(f -> f
                            .field("viewCount")
                            .order(SortOrder.Desc)
                    ));
                    break;
                default:
                    break;
            }

            // 1완성된 검색 요청(SearchRequest)을 Elasticsearch로 보내고, 결과를 받음
            SearchResponse<PostSearch> searchResponse = elasticsearchClient.search(builder.build(), PostSearch.class);

            // 결과에서 실제 게시글(PostSearch) 데이터만 추출해 리스트로 만듦
            List<PostSearch> posts = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
            // Spring의 Page 객체로 변환해서 반환 (페이징 처리 및 총 결과 수 포함)
            Page<PostSearch> postSearchPage = new PageImpl<>(posts, pageable, searchResponse.hits().total().value());
            return PostResponseConverter.toGetPostList(postSearchPage);
        } catch (IOException e) {
            logger.error("Elasticsearch 검색 중 오류 발생: {}", e.getMessage(), e);
            return PostResponseConverter.toGetPostList(Page.empty(pageable));
        }
    }

    /** 게시글 삭제 */
    public void delete(Long postId) {
        postSearchRepository.deleteById(Long.toHexString(postId));
    }

    private Query buildSearchQuery(String keyword) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        boolQuery.should(MatchQuery.of(m -> m.field("title").query(keyword))._toQuery());
        boolQuery.should(MatchQuery.of(m -> m.field("content").query(keyword))._toQuery());

        return boolQuery.build()._toQuery();
    }
}
