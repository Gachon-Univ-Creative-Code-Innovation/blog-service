package com.gucci.blog_service.userProfileCache.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String nickname;
    private String profileUrl;
}
