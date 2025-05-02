package com.gucci.blog_service.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.domain.dto.PostRequestDTO;
import com.gucci.blog_service.post.domain.dto.PostResponseDTO;
import com.gucci.blog_service.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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

    /**
     * 게시글
     */
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
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.postId").value(postId));
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updatePostTest() throws Exception {
        Long postId = 1L;
        PostRequestDTO.updatePost request = PostRequestDTO.updatePost.builder()
                .title("제목")
                .content("내용")
                .build();

        Post post = Post.builder()
                .postId(postId)
                .build();

        Mockito.when(postService.updatePost(anyString(), anyLong(), any())).thenReturn(post);

        mockMvc.perform(patch("/api/blog-service/posts/{postId}", postId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
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
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data").value("글이 정상적으로 삭제되었습니다."));
    }


    /**
     * 임시저장
     */
    @Test
    @DisplayName("임시저장 생성 테스트")
    void createDraftTest() throws Exception {
        PostRequestDTO.createDraft request = PostRequestDTO.createDraft.builder()
                .title("제목")
                .content("내용")
                .build();
        Post post = Post.builder()
                .postId(1L)
                .build();

        Mockito.when(postService.createDraft(anyString(), any())).thenReturn(post);

        mockMvc.perform(post("/api/blog-service/posts/drafts")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data").value(post.getPostId() + " 글이 임시저장되었습니다"));
    }

    @Test
    @DisplayName("임시저장 글 상세조회")
    void getDraftDetailTest() throws Exception {
        Long draftPostId = 1L;
        PostResponseDTO.GetDraftDetail response = PostResponseDTO.GetDraftDetail.builder()
                .draftPostId(draftPostId)
                .build();

        Mockito.when(postService.getDraftDetail(anyString(), anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/blog-service/posts/drafts/{draftPostId}", draftPostId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.draftPostId").value(response.getDraftPostId()));
    }

    @Test
    @DisplayName("임시저장 리스트 조회")
    void getDraftListTest() throws Exception {
        PostResponseDTO.GetDraft draft1 = PostResponseDTO.GetDraft.builder()
                .draftPostId(1L)
                .build();
        PostResponseDTO.GetDraft draft2 = PostResponseDTO.GetDraft.builder()
                .draftPostId(2L)
                .build();
        PostResponseDTO.GetDraftList draftList = PostResponseDTO.GetDraftList.builder()
                .draftList(List.of(draft1, draft2))
                .build();

        Mockito.when(postService.getDraftList(anyString())).thenReturn(draftList);

        mockMvc.perform(get("/api/blog-service/posts/drafts")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.draftList").isArray())
                .andExpect(jsonPath("$.data.draftList.length()").value(2))
                .andExpect(jsonPath("$.data.draftList[0].draftPostId").value(1L))
                .andExpect(jsonPath("$.data.draftList[1].draftPostId").value(2L));
    }

    @Test
    @DisplayName("임시저장 삭제 테스트")
    void deleteDraftTest() throws Exception {
        Long draftPostId = 1L;

        Mockito.doNothing().when(postService).deleteDraft(anyString(), anyLong());

        mockMvc.perform(delete("/api/blog-service/posts/drafts/{draftPostId}", draftPostId)
        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data").value("임시저장 글 삭제 완료"));

    }



}

