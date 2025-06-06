package com.gucci.blog_service.comment.service;

import com.gucci.blog_service.comment.domain.Comment;
import com.gucci.blog_service.comment.domain.dto.CommentRequestDTO;
import com.gucci.blog_service.comment.domain.dto.CommentResponseDTO;
import com.gucci.blog_service.comment.repository.CommentRepository;
import com.gucci.blog_service.global.JwtTokenHelper;
import com.gucci.blog_service.kafka.dto.NewCommentCreatedEvent;
import com.gucci.blog_service.kafka.dto.NewReplyCreatedEvent;
import com.gucci.blog_service.kafka.producer.BlogEventProducer;
import com.gucci.blog_service.post.domain.Post;
import com.gucci.blog_service.post.service.PostService;
import com.gucci.blog_service.userProfileCache.domain.UserProfile;
import com.gucci.blog_service.userProfileCache.service.UserProfileService;
import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PostService postService;
    private final UserProfileService userProfileService;

    private final CommentRepository commentRepository;
    private final JwtTokenHelper jwtTokenHelper;
    private final BlogEventProducer blogEventProducer;

    public Comment createComment(CommentRequestDTO.CreateComment createComment, String token) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);
        String userNickName = jwtTokenHelper.getNicknameFromToken(token);

        Post post = postService.getPostById(createComment.getPostId());
        Comment parentComment = null;
        if (createComment.getParentCommentId() != null) {
            parentComment = commentRepository.findById(createComment.getParentCommentId()).orElse(null);
        }

        Comment newComment = Comment.builder()
                .userId(userId)
                .post(post)
                .content(createComment.getContent())
                .parentComment(parentComment)
                .isDeleted(false)
                .build();


        Comment saved = commentRepository.save(newComment);

        if (parentComment != null) {
            // 새 답글 이벤트 발행
            Long parentUserId = parentComment.getUserId();
            if (!parentUserId.equals(userId)) {
                blogEventProducer.publishNewReplyEvent(
                        NewReplyCreatedEvent.builder()
                                .postId(saved.getPost().getPostId())
                                .receiverId(saved.getParentComment().getUserId())
                                .commenterId(userId)
                                .commenterNickname(userNickName)
                                .build()
                );
            }
        } else {
            // 새 댓글 이벤트 발행
            Long postAuthorId = post.getUserId();
            if (!postAuthorId.equals(userId)) {
                blogEventProducer.publishNewCommentEvent(
                        NewCommentCreatedEvent.builder()
                                .postId(post.getPostId())
                                .authorId(postAuthorId)
                                .commenterId(userId)
                                .commenterNickname(userNickName)
                                .build()
                );
            }
        }

        return saved;
    }

    // 포스트 별 댓글 조회
    public CommentResponseDTO.GetCommentList getCommentsByPostId(Long postId) {
        Post post = postService.getPostById(postId);
        List<Comment> allComments = commentRepository.findAllByPost(post);

        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .sorted(Comparator.comparing(Comment::getCreatedAt)) //처음 만들어진거 먼저 출력
                .toList();

        List<CommentResponseDTO.GetComment> result = new ArrayList<>();
        for (Comment root : rootComments) {
            buildCommentTree(result, root, 0);
        }

        return CommentResponseDTO.GetCommentList.builder()
                .commentList(result)
                .build();
    }

    private void buildCommentTree(List<CommentResponseDTO.GetComment> result, Comment comment, int depth) {
        UserProfile profile;
        if(comment.getIsDeleted()) {
            profile = UserProfile.builder()
                    .userId(null)
                    .nickname("알수없음")
                    .profileUrl("https://alog-profile-images.s3.ap-northeast-2.amazonaws.com/default_profile.png")
                    .build();
        }
        else {
            profile = userProfileService.getUserProfile(comment.getUserId());
        }
        CommentResponseDTO.GetComment dto = CommentResponseDTO.GetComment.builder()
                .commentId(comment.getCommentId())
                .parentCommentId(
                        Optional.ofNullable(comment.getParentComment())//부모 댓글이 있으면
                        .map(Comment::getCommentId) //가져오기
                        .orElse(null)) //아니면 null
                .content(comment.getContent())
                .createTime(comment.getCreatedAt())
                .updateTime(comment.getUpdatedAt())
                .authorNickname(profile.getNickname())
                .authorId(comment.getUserId())
                .authorProfileUrl(profile.getProfileUrl())
                .depth(depth)
                .isDeleted(comment.getIsDeleted())
                .build();

        result.add(dto);

        // 자식 댓글도 정렬해서 재귀 호출
        List<Comment> childComments = Optional.ofNullable(comment.getChildComments())
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .toList();

        for (Comment child : childComments) {
            buildCommentTree(result, child, depth + 1);
        }
    }


    @Transactional //JPA 영속성 컨텍스트라면 변경된 필드만 감지해서 업데이트 해준다
    public Comment updateComment(Long commentId, CommentRequestDTO.UpdateComment updateComment, String token) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));

        //권한체크
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        comment.updateContent(updateComment.getContent());
        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId, String token) {
        Long userId = jwtTokenHelper.getUserIdFromToken(token);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));

        //권한체크
        if (!comment.getUserId().equals(userId) && !comment.getPost().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        comment.setDeleted();
    }


}
