package com.gucci.blog_service.comment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.repository.CommentRepository;
import com.gucci.blog_service.config.JwtTokenHelper;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @MockitoBean
    private JwtTokenHelper jwtTokenHelper;

    private String token;
    private Long userId;

    @BeforeEach
    void setup() {
        userId = 1L;
        token = "Bearer test-token";
        //jwtTokenHelper test 용으로 구현
        Mockito.when(jwtTokenHelper.getUserIdFromToken(anyString())).thenReturn(userId);
    }

    @Test
    @DisplayName("댓글 생성 → 조회 → 수정 → 삭제까지 전체 플로우 테스트")
    void fullCommentFlowTest() throws Exception {
        // 1. 게시글 생성 (DB에 저장)
        Post post = Post.builder()
                .title("통합 테스트 게시글")
                .userId(userId)
                .documentId(1L)
                .view(0L)
                .build();
        postRepository.save(post);

        // 2. 댓글 생성
        CommentRequestDTO.CreateComment createDto = new CommentRequestDTO.CreateComment();
        createDto.setPostId(post.getPostId());
        createDto.setContent("통합 테스트 댓글");
        createDto.setParentCommentId(null);

        MvcResult createResult = mockMvc.perform(post("/api/blog-service/comments")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long commentId = extractCommentIdFromMessage(createResponse); // "1 댓글이 정상적으로 생성되었습니다" → 숫자 추출

        // 3. 댓글 조회
        mockMvc.perform(get("/api/blog-service/comments/{postId}", post.getPostId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].commentId").value(commentId))
                .andExpect(jsonPath("$.data[0].content").value("통합 테스트 댓글"))
                .andExpect(jsonPath("$.data[0].parentCommentId").value(nullValue()))
                .andExpect(jsonPath("$.data[0].depth").value(0));

        // 4. 댓글 수정
        CommentRequestDTO.UpdateComment updateDto = new CommentRequestDTO.UpdateComment();
        updateDto.setContent("수정된 댓글");

        mockMvc.perform(patch("/api/blog-service/comments/{commentId}", commentId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(commentId + " 댓글 정상적으로 업데이트를 완료했습니다"));

        // 5. 댓글 삭제
        mockMvc.perform(delete("/api/blog-service/comments/{commentId}", commentId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("댓글 삭제 완료"));


    }


    // 메시지에서 숫자(commentId) 추출
    private Long extractCommentIdFromMessage(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        String message = root.get("data").asText(); // 예: "1 댓글이 정상적으로 생성되었습니다"
        return Long.parseLong(message.split(" ")[0]);
    }

}
