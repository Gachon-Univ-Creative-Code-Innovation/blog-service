package com.gucci.blog_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCommentCreatedEvent {
    private Long postId;
    private Long authorId;
    private Long commenterId;
    private String commenterNickname;
}
