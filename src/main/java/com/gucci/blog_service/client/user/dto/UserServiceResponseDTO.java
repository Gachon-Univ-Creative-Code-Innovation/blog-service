package com.gucci.blog_service.client.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class UserServiceResponseDTO {
    @Getter
    @Builder
    public static class UserFollowingIds {
        List<Long> userIdList;
    }
}
