package com.gucci.blog_service.post.controller;

import com.gucci.blog_service.post.domain.dto.PresignedUrlResponseDTO;
import com.gucci.blog_service.post.service.S3Service;
import com.gucci.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog-service/s3")
public class S3Controller {

    private final S3Service s3Service;

    //클라이언트(프론트엔드)가 파일을 직접 업로드할 수 있는 URL을 발급
    @PostMapping("/upload-url")
    public ApiResponse<PresignedUrlResponseDTO> createPresignedUrl(@RequestParam String fileName) {
        PresignedUrlResponseDTO url = s3Service.generatePresignedUploadUrl(fileName);
        return ApiResponse.success(url);
    }

    @GetMapping("/image-url")
    public ApiResponse<String> getPresignedUrl(@RequestParam String objectUrl) {
        String presignedUrl = s3Service.getPresignedUrl(objectUrl);
        return ApiResponse.success(presignedUrl);
    }
}
