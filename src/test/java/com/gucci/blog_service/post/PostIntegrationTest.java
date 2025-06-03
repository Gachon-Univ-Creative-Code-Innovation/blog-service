package com.gucci.blog_service.post;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gucci.blog_service.global.JwtTokenHelper;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.repository.PostRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PostIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @MockitoBean
    private JwtTokenHelper jwtTokenHelper;

    private String token;
    private Long userId;
    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        userId = 1L;
        token = "Bearer test-token";
        //jwtTokenHelper test 용으로 구현
        Mockito.when(jwtTokenHelper.getUserIdFromToken(anyString())).thenReturn(userId);
    }

    @Test
    @DisplayName("임시저장 -> 임시저장 조회 -> 임시저장  -> 발행 -> 조회 -> 임시저장 -> 수정 -> 임시저장 -> 삭제")
    void fullDraftPostTest() throws Exception {
        // 1. 임시저장 생성
        PostRequestDTO.CreateDraft createDraft = PostRequestDTO.CreateDraft.builder()
                .draftPostId(null)
                .parentPostId(null)
                .title("임시저장 제목")
                .content("임시저장 내용")
                .tagNameList(List.of("tag1", "tag2"))
                .categoryCode(1L)
                .build();

        MvcResult createDraftResult = mockMvc.perform(post("/api/blog-service/posts/drafts")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDraft)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andReturn();
        String createDraftResponse = createDraftResult.getResponse().getContentAsString();
        Long draftId = extractPostIdFromMessage(createDraftResponse);

        // 2. 임시저장 조회
        mockMvc.perform(get("/api/blog-service/posts/drafts/{draftPostId}", draftId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.draftPostId").value(draftId))
                .andExpect(jsonPath("$.data.parentPostId").isEmpty())
                .andExpect(jsonPath("$.data.authorId").value(userId))
                .andExpect(jsonPath("$.data.title").value("임시저장 제목"))
                .andExpect(jsonPath("$.data.content").value("임시저장 내용"))
                .andExpect(jsonPath("$.data.tagNameList[0]").value("tag1"))
                .andExpect(jsonPath("$.data.tagNameList[1]").value("tag2"))
                .andExpect(jsonPath("$.data.categoryCode").value(1L));

        // 3. 임시저장 수정
        PostRequestDTO.CreateDraft createDraft2 = PostRequestDTO.CreateDraft.builder()
                .draftPostId(draftId)
                .parentPostId(null)
                .title("임시저장2 제목")
                .content("임시저장2 내용")
                .tagNameList(List.of("tag1", "tag3"))
                .categoryCode(2L)
                .build();
        MvcResult createDraftResult2 = mockMvc.perform(post("/api/blog-service/posts/drafts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDraft2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andReturn();

        String createDraftResponse2 = createDraftResult2.getResponse().getContentAsString();
        Long draft2Id = extractPostIdFromMessage(createDraftResponse2);

        assertThat(draft2Id).isEqualTo(draftId);

        // 3-2. 임시저장 조회
        mockMvc.perform(get("/api/blog-service/posts/drafts/{draftPostId}", draft2Id)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.draftPostId").value(draftId))
                .andExpect(jsonPath("$.data.parentPostId").isEmpty())
                .andExpect(jsonPath("$.data.authorId").value(userId))
                .andExpect(jsonPath("$.data.title").value("임시저장2 제목"))
                .andExpect(jsonPath("$.data.content").value("임시저장2 내용"))
                .andExpect(jsonPath("$.data.tagNameList[0]").value("tag1"))
                .andExpect(jsonPath("$.data.tagNameList[1]").value("tag3"))
                .andExpect(jsonPath("$.data.categoryCode").value(2L));

        // 4. 발행
        PostRequestDTO.CreatePost createPost = PostRequestDTO.CreatePost.builder()
                .draftPostId(draftId)
                .content("최종 게시글 내용")
                .title("최종 게시글 제목")
                .tagNameList(List.of("tag1", "tag4"))
                .build();

        MvcResult createPostResult = mockMvc.perform(post("/api/blog-service/posts")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andReturn();

        String createPostResponse = createPostResult.getResponse().getContentAsString();
        Long postId = extractPostIdFromMessage(createPostResponse);

        assertThat(postId).isEqualTo(draftId);

        // 5. 글 조회
        mockMvc.perform(get("/api/blog-service/posts/{postId}", postId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.title").value("최종 게시글 제목"))
                .andExpect(jsonPath("$.data.content").value("최종 게시글 내용"))
                .andExpect(jsonPath("$.data.tagNameList[0]").value("tag1"))
                .andExpect(jsonPath("$.data.tagNameList[1]").value("tag4"))
                .andExpect(jsonPath("$.data.categoryCode").value(10L));


        // 6. 임시저장
        PostRequestDTO.CreateDraft createDraft3 = PostRequestDTO.CreateDraft.builder()
                .draftPostId(null)
                .parentPostId(postId)
                .title("임시저장3 제목")
                .content("임시저장3 내용")
                .tagNameList(List.of("tag1", "tag3"))
                .build();
        MvcResult createDraftResult3 = mockMvc.perform(post("/api/blog-service/posts/drafts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDraft3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andReturn();

        String createDraftResponse3 = createDraftResult3.getResponse().getContentAsString();
        Long draft3Id = extractPostIdFromMessage(createDraftResponse3);

        //출력
        printPost();

        // 7. 임시저장 글로 게시글 수정
        PostRequestDTO.UpdatePost updatePost = PostRequestDTO.UpdatePost.builder()
//                .parentPostId(postId)
                .title("수정된 제목")
                .content("수정된 내용")
                .tagNameList(List.of("tag1", "tag3"))
                .categoryCode(3L)
                .build();

        MvcResult updateResult = mockMvc.perform(patch("/api/blog-service/posts/{postId}", postId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andReturn();

        String updatePostResponse = updateResult.getResponse().getContentAsString();
        Long updatedPostId = extractPostIdFromMessage(updatePostResponse);

        //출력
        printPost();

        assertThat(updatedPostId).isEqualTo(postId);



        // 8. 조회
        mockMvc.perform(get("/api/blog-service/posts/{postId}", updatedPostId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.postId").value(updatedPostId))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))
                .andExpect(jsonPath("$.data.tagNameList[0]").value("tag1"))
                .andExpect(jsonPath("$.data.tagNameList[1]").value("tag3"))
                .andExpect(jsonPath("$.data.categoryCode").value(3L));


        // 9. 임시저장
        PostRequestDTO.CreateDraft createDraft4 = PostRequestDTO.CreateDraft.builder()
                .draftPostId(null)
                .parentPostId(postId)
                .title("임시저장4 제목")
                .content("임시저장4 내용")
                .tagNameList(List.of("tag4"))
                .build();
        MvcResult createDraftResult4 = mockMvc.perform(post("/api/blog-service/posts/drafts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDraft4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andReturn();

        String createDraftResponse4 = createDraftResult4.getResponse().getContentAsString();
        Long draft4Id = extractPostIdFromMessage(createDraftResponse4);

        // 10. 삭제
        mockMvc.perform(delete("/api/blog-service/posts/{postId}", postId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

        printPost();
    }

    @Test
    @DisplayName("발행 -> 조회 -> 수정 -> 삭제")
    void fullPostTest() throws Exception {
        // 1. 발행
        PostRequestDTO.CreatePost createPost = PostRequestDTO.CreatePost.builder()
                .title("최초 발행 제목")
                .content("최초 발행 내용")
                .tagNameList(List.of("tag1", "tag2"))
                .categoryCode(1L)
                .build();

        MvcResult publishResult = mockMvc.perform(post("/api/blog-service/posts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andReturn();

        String publishResponse = publishResult.getResponse().getContentAsString();
        Long postId = extractPostIdFromMessage(publishResponse);

        // 2. 조회
        mockMvc.perform(get("/api/blog-service/posts/{postId}", postId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.title").value("최초 발행 제목"))
                .andExpect(jsonPath("$.data.content").value("최초 발행 내용"))
                .andExpect(jsonPath("$.data.tagNameList[0]").value("tag1"))
                .andExpect(jsonPath("$.data.tagNameList[1]").value("tag2"))
                .andExpect(jsonPath("$.data.categoryCode").value(1L));

        // 3. 수정
        PostRequestDTO.UpdatePost updatePost = PostRequestDTO.UpdatePost.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .tagNameList(List.of("tag1", "tag3"))
                .build();

        mockMvc.perform(patch("/api/blog-service/posts/{postId}", postId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

        // 3-1. 수정된 내용 다시 조회
        mockMvc.perform(get("/api/blog-service/posts/{postId}", postId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))
                .andExpect(jsonPath("$.data.tagNameList[0]").value("tag1"))
                .andExpect(jsonPath("$.data.tagNameList[1]").value("tag3"))
                .andExpect(jsonPath("$.data.categoryCode").value(10L)); // categoryCode : null request면 ETC(10L)할당

        // 4. 삭제
        mockMvc.perform(delete("/api/blog-service/posts/{postId}", postId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

    }

    private void printPost() {
        em.flush();  // 변경 사항을 DB에 반영
        em.clear();  // 1차 캐시 제거 → 이후 쿼리는 DB에서 다시 조회

        // 3. 실제 DB에서 조회
        List<Post> posts = postRepository.findAll();
        System.out.println(">> 리스트 사이즈: " + posts.size());

        for (Post post : posts) {
            System.out.println(">> DB에서 조회한 제목: " + post.getTitle());
            System.out.println(">> DB에서 조회한 id: " + post.getPostId());
        }
    }

    // 메시지에서 숫자(draftPostId) 추출
    private Long extractPostIdFromMessage(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        String message = root.get("data").asText(); // 예: "1 댓글이 정상적으로 생성되었습니다"
        return Long.parseLong(message.split(" ")[0]);
    }
}
