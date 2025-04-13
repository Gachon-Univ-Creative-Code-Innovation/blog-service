package com.gucci.blog_service.comment.service;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.repository.CommentRepository;
import com.gucci.blog_service.config.JwtTokenHelper;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.service.PostService;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class) //Mockito와 Junit통합
public class CommentServiceTest {
    @InjectMocks //테스트 할 객체
    private CommentService commentService;

    @Mock //의존 객체
    private PostService postService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private JwtTokenHelper jwtTokenHelper;

    private final Long userId = 1L;
    private final String token = "Bearer test-token";


    @Test
    @DisplayName("댓글 생성 - root 댓글")
    void createRootCommentTest() {
        //given
        Long postId = 1L;
        Post post = Post.builder()
                .postId(postId)
                .userId(userId)
                .build();

        CommentRequestDTO.CreateComment dto = new CommentRequestDTO.CreateComment();
        dto.setPostId(postId);
        dto.setContent("댓글 내용");
        dto.setParentCommentId(null);

        Comment comment = Comment.builder()
                .commentId(1L)
                .userId(userId)
                .content(dto.getContent())
                .post(post)
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(postService.getPostById(postId)).thenReturn(post);
        Mockito.when(commentRepository.save(any())).thenReturn(comment);

        Comment result = commentService.createComment(dto, token);

        assertThat(result.getCommentId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo(dto.getContent());
    }

    @Test
    @DisplayName("댓글 수정")
    void updateCommentSuccessTest(){
        Long commentId = 1L;
        Comment comment = Comment.builder()
                .commentId(commentId)
                .userId(userId)
                .content("old")
                .build();


        CommentRequestDTO.UpdateComment dto = new CommentRequestDTO.UpdateComment();
        dto.setContent("수정된 내용");

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Comment result = commentService.updateComment(commentId, dto, token);

        assertThat(result.getCommentId()).isEqualTo(commentId);
        assertThat(result.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("댓글 수정 - 존재하지 않는 댓글")
    void updateCommentNotFoundTest(){
        Long commentId = 999L;

        CommentRequestDTO.UpdateComment dto = new CommentRequestDTO.UpdateComment();
        dto.setContent("수정된 내용");

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.updateComment(commentId, dto, token))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_ARGUMENT.getMessage());
    }

    @Test
    @DisplayName("댓글 수정 - 권한 없음")
    void updateCommentNotPermissionTest(){
        Long commentId = 1L;
        Comment comment = Comment.builder()
                .commentId(commentId)
                .userId(2L) // 다른 유저 ID
                .build();
        CommentRequestDTO.UpdateComment dto = new CommentRequestDTO.UpdateComment();
        dto.setContent("수정");

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.updateComment(commentId, dto, token))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_ARGUMENT.getMessage());
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteCommentTest() {
        Long commentId = 1L;
        Post post = Post.builder().userId(userId).build();
        Comment comment = Comment.builder()
                .commentId(commentId)
                .userId(userId)
                .post(post)
                .isDeleted(false)
                .content("댓글 내용")
                .build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        commentService.deleteComment(commentId, token);

        // then
        assertThat(comment.getIsDeleted()).isTrue();
        assertThat(comment.getUserId()).isNull();
        assertThat(comment.getContent()).isEqualTo("삭제된 댓글입니다.");
    }

    @Test
    @DisplayName("댓글 삭제 - 권한 없음")
    void deleteCommentNoPermissionTest() {
        Long commentId = 1L;
        Post post = Post.builder().userId(99L).build(); // 글쓴이도 아님
        Comment comment = Comment.builder().commentId(commentId).userId(2L).post(post).build();

        Mockito.when(jwtTokenHelper.getUserIdFromToken(token)).thenReturn(userId);
        Mockito.when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(commentId, token))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_ARGUMENT.getMessage());
    }




}
