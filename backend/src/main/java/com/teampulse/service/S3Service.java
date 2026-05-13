package com.teampulse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Presigner presigner;
    private final String bucketName;
    private final String region;
    private final int urlExpiration;

    public S3Service(
            S3Presigner presigner,
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.presigned-url-expiration}") int urlExpiration) {
        this.presigner = presigner;
        this.bucketName = bucketName;
        this.region = region;
        this.urlExpiration = urlExpiration;
    }

    public Map<String, String> generatePresignedUploadUrl(String fileName, String contentType) {
        String key = "avatars/" + UUID.randomUUID() + "/" + fileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(builder ->
                builder.signatureDuration(Duration.ofSeconds(urlExpiration))
                        .putObjectRequest(objectRequest));

        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);

        return Map.of(
                "uploadUrl", presignedRequest.url().toString(),
                "fileUrl", fileUrl
        );
    }
}
