package com.gucci.blog_service.kafka.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewPostCreatedEvent {
    private Long postId;
    private Long authorId;
    private String authorNickname;
}
