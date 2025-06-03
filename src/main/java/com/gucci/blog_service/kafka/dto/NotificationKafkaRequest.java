package com.gucci.blog_service.kafka.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationKafkaRequest {
    private Long receiverId;
    private Long senderId;
    private String type;        // 예: FOLLOW
    private String content;     // 예: OO가 당신을 팔로우 했습니다.
    private String targetUrl;   // 예: /user/2 or /blog/2 (클릭 시 이동 될 url)
}
