package com.gucci.blog_service.post.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.gucci.blog_service.comment.service.CommentRefService;
import com.gucci.blog_service.comment.service.CommentService;
import com.gucci.blog_service.post.converter.PostResponseConverter;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.PostDocument;
import com.gucci.blog_service.post.domain.PostSearch;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.domain.enums.PostType;
import com.gucci.blog_service.post.repository.PostRepository;
import com.gucci.blog_service.post.repository.PostSearchRepository;
import com.gucci.blog_service.tag.service.TagService;
import com.gucci.blog_service.userProfileCache.domain.UserProfile;
import com.gucci.blog_service.userProfileCache.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostSearchService {
    private final PostSearchRepository postSearchRepository;
    private final TagService tagService;
    private final S3Service s3Service;
    private final UserProfileService userProfileService;
    private final CommentRefService commentRefService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private final Logger logger = LoggerFactory.getLogger(PostSearchService.class);
    @Autowired
    private PostRepository postRepository;

    /** mongodb post -> elasticsearch에 인덱싱 */
    public void index(Post post, PostDocument postDocument, Set<String> tags) {
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
                .postType(post.getPostType())
                .build();

        postSearchRepository.save(postSearch);
    }

    /** 게시글 업데이트 시 반영 */
    public void update(Post post, PostDocument postDocument, Set<String> tags) {
        //같은 id를 가지면 업데이트 됨
        index(post, postDocument, tags);
    }

    /** 조회수 반영 */
    public void updateViewCount(Long postId, Long viewCount) {
        try {
            UpdateRequest<Map<String, Object>, Map<String, Object>> request = UpdateRequest.of(u -> u
                    .index("post")
                    .id(Long.toHexString(postId))
                    .doc(Map.of("viewCount", viewCount))
            );
            elasticsearchClient.update(request, Map.class);

        }catch (IOException e) {
            logger.error("Elasticsearch 조회수 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }

    }

    /** 닉네임 동기화 */
    public void updateUserNickname(List<String> postIds, String userNickName) {
        try {

            // 1) 각 ID마다 BulkOperation.update(...)를 만든다
            List<BulkOperation> ops = postIds.stream()
                    .map(hexId -> BulkOperation.of(op -> op
                            .update(u -> u
                                    .index("post")
                                    .id(hexId)
                                    .action(a -> a.doc(Map.of("author", userNickName)))
                            )
                    ))
                    .toList();

            // 2) BulkRequest 에 묶어서 전송
            BulkRequest bulkReq = BulkRequest.of(b -> b
                    .operations(ops)
            );

            // 3) 실제 호출
            BulkResponse bulkResp = elasticsearchClient.bulk(bulkReq);

            // 4) 실패 항목 로깅
            if (bulkResp.errors()) {
                bulkResp.items().forEach(item -> {
                    if (item.error() != null) {
                        logger.error("Failed to update id={} : {}",
                                item.id(), item.error().reason());
                    }
                });
            }
        }catch (IOException e) {
            logger.error("Elasticsearch 조회수 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    public PostResponseDTO.GetPostList search(String token, String keyword, PostType postType, Integer sortBy, Integer page) {
        // init
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<PostResponseDTO.GetPost> dtoList = List.of();
        Page<PostSearch> postSearchPage = Page.empty();


        try{
            Query query = buildSearchQuery(keyword, postType);
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

            // 1. 완성된 검색 요청(SearchRequest)을 Elasticsearch로 보내고, 결과를 받음
            SearchResponse<PostSearch> searchResponse = elasticsearchClient.search(builder.build(), PostSearch.class);

            // 2. 결과에서 실제 게시글(PostSearch) 데이터만 추출해 리스트로 만듦
            List<PostSearch> searchPosts = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            // 3. Post 조회
            List<Long> postIds = searchPosts.stream().map( s -> Long.parseLong(s.getPostId(), 16)).toList();
            List<Post> posts = postRepository.findAllById(postIds);
            Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getPostId, Function.identity()));

            // 4. converter로 넘겨 DTO 매핑
            dtoList = searchPosts.stream().map(sp -> {
                        Long id = Long.parseLong(sp.getPostId(), 16);
                        Post post = postMap.get(id);
                        if (post == null) {
                            return null;
                        }
                        else {
                            String thumbnail = s3Service.getPresignedUrl(post.getThumbnail());
                            UserProfile profile = userProfileService.getUserProfile(token, post.getUserId());
                            Integer commentCount = commentRefService.getCommentCount(post);

                            return PostResponseConverter.toGetPostDto(post, thumbnail, profile, commentCount);
                        }
                    })
                    .toList();

            //5. 페이징 DTO 생성
            // Spring 의 Page 객체로 변환해서 반환 (페이징 처리 및 총 결과 수 포함)
            postSearchPage = new PageImpl<>(searchPosts, pageable, searchResponse.hits().total().value());
            return PostResponseConverter.toGetPostList(postSearchPage, dtoList);
        } catch (IOException e) {
            logger.error("Elasticsearch 검색 중 오류 발생: {}", e.getMessage(), e);
            return PostResponseConverter.toGetPostList(postSearchPage, dtoList);
        }
    }

    /** 게시글 삭제 */
    public void delete(Long postId) {
        postSearchRepository.deleteById(Long.toHexString(postId));
    }

    private Query buildSearchQuery(String keyword, PostType postType) {
        return Query.of(q -> q.bool(b ->
                b.should(s ->
                        s.multiMatch(mm ->
                                mm.fields("title", "content")
                                        .query(keyword)
                        ))
                        .minimumShouldMatch("1")
                        .filter(f ->
                                f.term(t->
                                        t.field("postType")
                                                .value(postType.name())
                                ))
        ));

    }
}
