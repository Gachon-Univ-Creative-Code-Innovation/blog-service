package com.gucci.blog_service.userProfileCache.service;

import com.gucci.blog_service.client.user.client.UserServiceClient;
import com.gucci.blog_service.global.JwtTokenHelper;
import com.gucci.blog_service.userProfileCache.domain.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * UserProfileService:
 *  - Redis 캐시에 사용자 프로필을 저장하고 조회하는 역할
 *  - @Cacheable: 캐시 조회 (없으면 user-service 호출 → 캐시에 저장)
 *  - @CachePut: 캐시 업데이트 (user-service에서 바뀐 프로필을 덮어쓰기)
 */
@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserServiceClient userServiceClient;


    /**
     * 캐시된 userProfile이 없으면 user-service를 호출해 가져온 후 Redis에 저장한다.
     * cacheNames="userProfile": Redis에 저장될 캐시 이름
     * key="#userId": 캐시 키를 userId로 지정
     */
    @Cacheable(cacheNames = "userProfile", key = "#userId")
    public UserProfile getUserProfile(String token, Long userId) {
        // 캐시에 없으면 user-service API 호출
        return userServiceClient.getUserProfile(userId);
    }

    /**
     * PATCH 호출 등으로 user-service에서 들어온 변경된 UserProfile을
     * 바로 Redis 캐시에 저장(덮어쓰기)하기 위해 사용
     * @CachePut: 메서드가 호출될 때 반환값을 Redis에 저장한다.
     */
    @CachePut(cacheNames = "userProfile", key = "#userProfile.userId")
    public UserProfile putUserProfile(UserProfile userProfile) {
        return userProfile;
    }
}