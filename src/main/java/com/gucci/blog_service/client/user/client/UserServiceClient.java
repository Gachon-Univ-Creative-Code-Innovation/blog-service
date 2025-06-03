package com.gucci.blog_service.client.user.client;

import com.gucci.blog_service.client.user.api.UserServiceAPI;
import com.gucci.blog_service.client.user.dto.UserServiceResponseDTO;
import com.gucci.blog_service.userProfileCache.domain.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {
    private final UserServiceAPI userServiceAPI;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Long> getUserFollowingIds(String token) {
        UserServiceResponseDTO.UserFollowingIds res = userServiceAPI.getUserFollowingId(token);
        return res.getUserIdList();
    }

    public UserProfile getUserProfile(String token, Long userId) {
        logger.debug("debug 시작");

        UserServiceResponseDTO.UserProfileDto res = userServiceAPI.getUserProfile(token);
        logger.debug("debug "+res.getUserProfile().getNickname());
        return UserProfile.builder()
                .userId(userId)
                .nickname(res.getUserProfile().getNickname())
                .profileUrl(res.getUserProfile().getProfileUrl())
                .build();
    }
}
