package com.gucci.blog_service.client.user.api;

import com.gucci.blog_service.client.user.config.UserServiceFeignConfig;
import com.gucci.blog_service.client.user.dto.UserServiceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "user-service-client",
        url = "${feign.user-service.url}",
        configuration = UserServiceFeignConfig.class
)
public interface UserServiceAPI {
    @GetMapping(value = "/follow/followees")
    UserServiceResponseDTO.UserFollowingIds getUserFollowingId(
            @RequestHeader("Authorization") String token
    );

    @GetMapping(value = "/profile-nickname/{userId}")
    UserServiceResponseDTO.UserProfileDto getUserProfile(
            @PathVariable("userId") Long userId
    );
}
