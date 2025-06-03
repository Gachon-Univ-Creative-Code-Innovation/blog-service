package com.gucci.blog_service.userProfileCache.controller;

import com.gucci.blog_service.userProfileCache.domain.UserProfile;
import com.gucci.blog_service.userProfileCache.service.UserProfileService;
import com.gucci.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/blog-service/cache/user")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * PATCH 요청으로 들어온 UserProfile을 Redis에 덮어쓴다.
     * - 경로: PATCH /blog-service/cache/user/{userId}
     * - Body: { "userId":1234, "nickname":"newNick", "avatarUrl":"https://..." }
     */
    @Operation(summary = "유저 캐싱", description = "유저 캐시 업데이트")
    @PatchMapping("")
    public ApiResponse<UserProfile> updateUserProfileCache(
            @RequestBody UserProfile userProfile
    ) {
        // Redis 캐시에 덮어쓰기
        UserProfile updated = userProfileService.putUserProfile(userProfile);
        return ApiResponse.success(updated);
    }
}