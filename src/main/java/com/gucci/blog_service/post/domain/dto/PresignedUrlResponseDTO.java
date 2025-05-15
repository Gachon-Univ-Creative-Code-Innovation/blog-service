package com.gucci.blog_service.post.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PresignedUrlResponseDTO {
    private String presignedUrl;
    private String s3ObjectUrl;
}
