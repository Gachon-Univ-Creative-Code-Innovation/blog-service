package com.gucci.blog_service.client.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class UserServiceResponseDTO {
    @Getter
    @Setter
    public static class UserFollowingIds {
        String status;
        String message;
        @JsonProperty("data")
        List<Long> userIdList;
    }


    @Getter
    @Setter
    public static class UserProfileDto {
        String status;
        String message;
        @JsonProperty("data")
        UserProfile userProfile;
    }

    @Getter
    @Setter
    public static class UserProfile {
        String nickname;
        String profileUrl;
    }
}
