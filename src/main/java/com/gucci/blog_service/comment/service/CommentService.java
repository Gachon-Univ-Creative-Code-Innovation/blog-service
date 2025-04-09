package com.gucci.blog_service.comment.service;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.domain.dto.CommentResponseDTO;
import com.gucci.blog_service.comment.repository.CommentRepository;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.service.PostService;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PostService postService;
    private final CommentRepository commentRepository;

    public Comment createComment(CommentRequestDTO.CreateComment createComment) {
        Post post = postService.getPostById(createComment.getPostId());
        Comment parentComment = commentRepository.findById(createComment.getParentCommentId()).orElse(null);

        Comment newComment = Comment.builder()
                .post(post)
                .content(createComment.getContent())
                .parentComment(parentComment)
                .build();

        return commentRepository.save(newComment);
    }

    // 포스트 별 댓글 조회
    public List<CommentResponseDTO.GetComments> getCommentsByPostId(Long postId) {
        Post post = postService.getPostById(postId);
        List<Comment> allComments = commentRepository.findAllByPost(post);

        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .sorted(Comparator.comparing(Comment::getCreatedAt)) //처음 만들어진거 먼저 출력
                .toList();

        List<CommentResponseDTO.GetComments> result = new ArrayList<>();
        for (Comment root : rootComments) {
            buildCommentTree(result, root, 0);
        }

        return result;
    }

    private void buildCommentTree(List<CommentResponseDTO.GetComments> result, Comment comment, int depth) {
        CommentResponseDTO.GetComments dto = CommentResponseDTO.GetComments.builder()
                .commentId(comment.getCommentId())
                .parentCommentId(
                        Optional.ofNullable(comment.getParentComment())//부모 댓글이 있으면
                        .map(Comment::getCommentId) //가져오기
                        .orElse(null)) //아니면 null
                .content(comment.getContent())
                .createTime(comment.getCreatedAt())
                .updateTime(comment.getUpdatedAt())
                .authorNickname("임시")
                .authorId(0L)
                .depth(depth)
                .isDeleted(comment.getIsDeleted())
                .build();

        result.add(dto);

        // 자식 댓글도 정렬해서 재귀 호출
        List<Comment> childComments = comment.getChildComments().stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .toList();

        for (Comment child : childComments) {
            buildCommentTree(result, child, depth + 1);
        }
    }


    @Transactional //JPA 영속성 컨텍스트라면 변경된 필드만 감지해서 업데이트 해준다
    public Comment updateComment(Long commentId, CommentRequestDTO.UpdateComment updateComment) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ARGUMENT)); //commentId에 해당하는 댓글이 없음

        comment.updateContent(updateComment.getContent());
        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ARGUMENT));

        comment.setDeleted();
    }
}
