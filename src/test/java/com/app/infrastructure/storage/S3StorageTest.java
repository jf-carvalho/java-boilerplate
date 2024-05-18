package com.app.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class S3StorageTest {
    private S3Client s3Client;
    private S3Storage s3Storage;

    private final String bucketName = "test-bucket";
    private final String endpoint = "https://s3.amazonaws.com";
    private final String secretKey = "testSecretKey";
    private final String accessKey = "testAccessKey";


    @Test
    public void shouldCreateNewS3ClientInstance() {
        try (MockedStatic<StaticCredentialsProvider> staticCredentialsProviderMock = Mockito.mockStatic(StaticCredentialsProvider.class);
             MockedStatic<AwsBasicCredentials> awsBasicCredentialsMock = Mockito.mockStatic(AwsBasicCredentials.class)) {

            AwsBasicCredentials awsBasicCredentials = mock(AwsBasicCredentials.class);
            StaticCredentialsProvider staticCredentialsProvider = mock(StaticCredentialsProvider.class);

            S3ClientBuilder s3ClientBuilder = mock(S3ClientBuilder.class);
            S3Client s3Client = mock(S3Client.class);

            awsBasicCredentialsMock.when(() -> AwsBasicCredentials.create(accessKey, secretKey)).thenReturn(awsBasicCredentials);
            staticCredentialsProviderMock.when(() -> StaticCredentialsProvider.create(awsBasicCredentials)).thenReturn(staticCredentialsProvider);

            when(s3ClientBuilder.endpointOverride(URI.create(endpoint))).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.credentialsProvider(staticCredentialsProvider)).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.region(Region.US_EAST_1)).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.build()).thenReturn(s3Client);

            S3Storage newS3Storage = new S3Storage(accessKey, secretKey, endpoint, bucketName);

            S3Client result = newS3Storage.getS3Client();

            awsBasicCredentialsMock.verify(() -> AwsBasicCredentials.create(accessKey, secretKey), times(2));
            staticCredentialsProviderMock.verify(() -> StaticCredentialsProvider.create(awsBasicCredentials), times(2));

            assertNotNull(result);
        }
    }

    @Nested
    public class NestedS3StorageTests {

        @BeforeEach
        public void init() {
            s3Client = mock(S3Client.class);
            s3Storage = new S3Storage(accessKey, secretKey, endpoint, bucketName) {
                @Override
                S3Client getS3Client() {
                    return s3Client;
                }
            };
        }

        @Test
        public void shouldPutObjectIntoS3Bucket() {
            File file = new File("test.txt");
            Path filePath = file.toPath();

            String result = s3Storage.put(file);

            ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
            ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);

            verify(s3Client).putObject(putObjectRequestCaptor.capture(), pathCaptor.capture());

            PutObjectRequest putObjectRequest = putObjectRequestCaptor.getValue();
            Path capturedPath = pathCaptor.getValue();

            assertEquals(bucketName, putObjectRequest.bucket());
            assertEquals("test.txt", putObjectRequest.key());
            assertEquals(filePath, capturedPath);
            assertEquals(endpoint + "/" + bucketName + "/" + "test.txt", result);
        }

        @Test
        public void shouldDeleteObjectFromS3Bucket() {
            S3Utilities s3Utilities = mock(S3Utilities.class);
            when(s3Client.utilities()).thenReturn(s3Utilities);

            String fileUrl = "http://localhost:4566/test-bucket/test.txt";
            URI uri = URI.create(fileUrl);
            S3Uri s3Uri = mock(S3Uri.class);

            when(s3Client.utilities().parseUri(uri)).thenReturn(s3Uri);
            when(s3Uri.bucket()).thenReturn(Optional.of("test-bucket"));
            when(s3Uri.key()).thenReturn(Optional.of("test.txt"));

            boolean result = s3Storage.delete(fileUrl);

            ArgumentCaptor<DeleteObjectRequest> deleteObjectRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
            verify(s3Client).deleteObject(deleteObjectRequestCaptor.capture());

            DeleteObjectRequest deleteObjectRequest = deleteObjectRequestCaptor.getValue();

            assertEquals("test-bucket", deleteObjectRequest.bucket());
            assertEquals("test.txt", deleteObjectRequest.key());
            assertTrue(result);
        }
    }
}