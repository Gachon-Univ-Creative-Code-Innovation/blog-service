package com.gucci.blog_service.post.service;

import com.gucci.blog_service.config.S3Config;
import com.gucci.blog_service.post.domain.dto.PresignedUrlResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private static final String FOLDER_PREFIX = "post/";
    private static final long PRESIGNED_EXPIRATION = 30;

    private final S3Presigner s3Presigner;
    private final S3Config s3Config;
    private final S3Client s3Client;

    public PresignedUrlResponseDTO generatePresignedUploadUrl(String fileName) {
        String bucketName = s3Config.getBucketName(); //s3 버킷 이름 불러오기

        String objectKey = FOLDER_PREFIX + UUID.randomUUID() + "-" + fileName; //파일 명 생성

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(PRESIGNED_EXPIRATION)) //유효 시간 설정
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return PresignedUrlResponseDTO.builder()
                .presignedUrl(presignedRequest.url().toString())
                .s3ObjectUrl(objectKey)
                .build();
    }


    public String getPresignedUrl(String objectUrl) {
        String bucketName = s3Config.getBucketName(); //버킷 이름 가져오기

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectUrl)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(PRESIGNED_EXPIRATION)) //유효시간 설정
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }

    public void deleteFile(String objectKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}
