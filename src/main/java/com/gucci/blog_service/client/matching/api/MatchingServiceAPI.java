package com.gucci.blog_service.client.matching.api;

import com.gucci.blog_service.client.matching.config.MatchingServiceFeignConfig;
import com.gucci.blog_service.client.matching.dto.MatchingServiceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "matching-service",
        url = "${feign.matching-service.url}",
        configuration = MatchingServiceFeignConfig.class
)
public interface MatchingServiceAPI {

    @GetMapping("/represent-tags")
    MatchingServiceResponseDTO.RepresentTags getRepresentTags(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam("userID") Long userId,
            @RequestParam("topK") Integer topK
    );
}
