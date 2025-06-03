package com.gucci.blog_service.kafka.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    POST("POST","님이 새 글을 작성했습니다."),
    COMMENT("COMMENT","님이 댓글을 남겼습니다."),
    REPLY("REPLY", "님이 답글을 남겼습니다.");

    private final String type;
    private final String message;
}