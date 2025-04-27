package com.gucci.blog_service.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@ActiveProfiles("test")
public class PostControllerTest {
    @MockitoBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;// Application @EnableJpaAuditing

    private final String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikh5ZW9uamkiLCJpYXQiOjE1MTYyMzkwMjIsInVzZXJfaWQiOjF9.UnBPcqiYQW7jQsP23-w5FwOfi1yj2VImffJl6nq_nZY";

    @Test
    @DisplayName("게시글 생성 테스트")
    void createPostTest() throws Exception {
        //given
        PostRequestDTO.createPost dto = PostRequestDTO.createPost.builder()
                .content("내용")
                .title("제목")
                .build();
        Post mockPost = Post.builder()
                .postId(1L)
                .title(dto.getTitle())
                .build();

        Mockito.when(postService.createPost(anyString(), any())).thenReturn(mockPost);

        mockMvc.perform(post("/api/blog-service/posts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data").value("1 글이 정상적으로 생성되었습니다."));
    }

    @Test
    @DisplayName("게시글 상세 조회 테스트")
    void getPostDetailTest() throws Exception {
        Long postId = 1L;
        PostResponseDTO.GetPostDetail dto = PostResponseDTO.GetPostDetail.builder()
                .postId(postId)
                .build();

        Mockito.when(postService.getPostDetail(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/api/blog-service/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.postId").value(postId));
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updatePostTest() throws Exception {
        PostRequestDTO.updatePost request = PostRequestDTO.updatePost.builder()
                .postId(1L)
                .title("제목")
                .content("내용")
                .build();

        Post post = Post.builder()
                .postId(request.getPostId())
                .build();

        Mockito.when(postService.updatePost(anyString(), any())).thenReturn(post);

        mockMvc.perform(patch("/api/blog-service/posts")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").value(post.getPostId()+" 글이 정상적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deletePostTest() throws Exception {
        Long postId = 1L;

        Mockito.doNothing().when(postService).deletePost(anyString(), anyLong());

        mockMvc.perform(delete("/api/blog-service/posts/{postId}", postId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").value("글이 정상적으로 삭제되었습니다."));
    }

}

