package com.gucci.blog_service.comment.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Comments;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponseDTO {

    @Getter
    @Builder
    public static class GetComment{
        Long commentId;
        @Schema(description = "root 댓글일 경우 null이 반환됩니다. 대댓글일 경우 부모 댓글이 반환됩니다.")
        Long parentCommentId;
        Long authorId;
        String authorNickname;
        String content;
        @Schema(description = "댓글의 계층입니다. root 댓글부터 0,1,2...로 증가합니다.")
        Integer depth;
        @Schema(description = "댓글의 삭제여부입니다. 삭제되었을 경우 content에 \"삭제된 댓글입니다\"라고 표시됩니다.")
        Boolean isDeleted;
        LocalDateTime createTime;
        LocalDateTime updateTime;
    }

    @Getter
    @Builder
    public static class GetCommentList{
        List<GetComment> commentList;
    }

}
