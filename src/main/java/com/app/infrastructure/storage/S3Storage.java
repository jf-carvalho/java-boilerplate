package com.app.infrastructure.storage;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

public class S3Storage implements StorageInterface {
    private S3Client s3Client;

    private final String accessKey;
    private final String secretKey;
    private final String endpoint;
    private final String bucketName;
    private final Region region;

    public S3Storage(String accessKey, String secretKey, String endpoint, String bucketName) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.region = Region.US_EAST_1;

        this.s3Client = this.getS3Client();
    }

    S3Client getS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(region)
                .build();
    }

    public String put(File file) {
        Path filePath = file.toPath();
        String key = file.getName();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(this.bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, filePath);

        return this.endpoint + File.separator + this.bucketName + File.separator + key;
    }

    public boolean delete(String fileUrl) {
        URI uri = URI.create(fileUrl);
        S3Uri s3Uri = s3Client.utilities().parseUri(uri);

        Optional<String> optionalBucket = s3Uri.bucket();
        String bucket = optionalBucket.orElse(this.bucketName);

        Optional<String> optionalKey = s3Uri.key();
        String key = optionalKey.orElse("");

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        s3Client.deleteObject(deleteObjectRequest);

        return true;
    }
}
