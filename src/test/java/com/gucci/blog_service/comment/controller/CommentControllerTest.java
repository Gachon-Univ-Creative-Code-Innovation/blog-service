package com.gucci.blog_service.comment.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.domain.dto.CommentResponseDTO;
import com.gucci.blog_service.comment.service.CommentService;
import com.gucci.blog_service.post.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
public class CommentControllerTest {
    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper; //DTO를 JSON으로 직렬화하거나 역직렬화할 때 사용

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext; // Application @EnableJpaAuditing

    @Test
    @DisplayName("댓글 생성 테스트 - root 댓글")
    void createRootCommentTest() throws Exception {
        //given
        CommentRequestDTO.CreateComment request = new CommentRequestDTO.CreateComment();
        request.setPostId(1L);
        request.setContent("댓글 내용");
        request.setParentCommentId(null);

        Comment mockComment = Comment.builder()
                .commentId(1L)
                .content("테스트 댓글 내용")
                .isDeleted(false)
                .build();

        Mockito.when(commentService.createComment(any(), anyString())).thenReturn(mockComment);

        mockMvc.perform(post("/api/blog-service/comments")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data").value("1 댓글이 정상적으로 생성되었습니다"));
    }


    @Test
    @DisplayName("댓글 목록 조회 테스트")
    void getCommentsTest() throws Exception {
        CommentResponseDTO.GetComments comment1 = CommentResponseDTO.GetComments.builder()
                .commentId(1L)
                .content("내용 1")
                .authorNickname("작성자 1")
                .build();
        CommentResponseDTO.GetComments comment2 = CommentResponseDTO.GetComments.builder()
                .commentId(2L)
                .content("내용 2")
                .authorNickname("작성자 2")
                .build();;

        Mockito.when(commentService.getCommentsByPostId(1L)).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/api/blog-service/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("데이터 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].commentId").value(1L))
                .andExpect(jsonPath("$.data[1].commentId").value(2L));
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void modifyCommentTest() throws Exception {
        CommentRequestDTO.UpdateComment request = new CommentRequestDTO.UpdateComment();
        request.setContent("수정된 댓글");

        Comment updatedComment = Comment.builder()
                .commentId(5L)
                .build();
        Mockito.when(commentService.updateComment(anyLong(), any(), anyString())).thenReturn(updatedComment);

        mockMvc.perform(patch("/api/blog-service/comments/5")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("데이터 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data").value("5 댓글 정상적으로 업데이트를 완료했습니다"));
    }


    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteCommentTest() throws Exception {
        Mockito.doNothing().when(commentService).deleteComment(anyLong(), anyString());

        mockMvc.perform(delete("/api/blog-service/comments/7")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("댓글 삭제 완료"));
    }
}

