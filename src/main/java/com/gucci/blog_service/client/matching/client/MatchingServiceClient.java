package com.gucci.blog_service.client.matching.client;

import com.gucci.blog_service.client.matching.api.MatchingServiceAPI;
import com.gucci.blog_service.client.matching.dto.MatchingServiceResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingServiceClient {
    private final MatchingServiceAPI matchingServiceAPI;
    private final Logger logger = LoggerFactory.getLogger(MatchingServiceClient.class);

    public List<String> getUserRepresentTags(String token, Long userId) {
        Integer topK = 5;
        MatchingServiceResponseDTO.RepresentTags res = matchingServiceAPI.getRepresentTags(token, userId, topK);
        return res.getTags();
    }
}
