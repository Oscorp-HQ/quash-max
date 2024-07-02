package com.quashbugs.quash.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quashbugs.quash.repo.*;
import com.quashbugs.quash.util.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;

import static com.quashbugs.quash.constants.Constants.MAX_RETRIES;

@Service
public class AwsStorageService extends AbstractStorageService {

    private final AmazonS3 s3Client;

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsStorageService.class);

    @Autowired
    public AwsStorageService(BugMediaRepository bugMediaRepository,
                             GifMediaRepository gifMediaRepository,
                             CrashLogRepository crashLogRepository,
                             ApplicationRepository applicationRepository,
                             ChatUploadRepository chatUploadRepository,
                             StorageProperties storageProperties) {
        super(bugMediaRepository, gifMediaRepository, crashLogRepository, applicationRepository, chatUploadRepository, storageProperties);

        try {
            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(storageProperties.getAwsRegion())
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(storageProperties.getAwsAccessKey(), storageProperties.getAwsSecretKey())))
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error initializing AwsStorageService", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void uploadWithRetries(byte[] content, String objectName, String mimeType) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(mimeType);
                s3Client.putObject(storageProperties.getAwsBucketName(), objectName, new ByteArrayInputStream(content), metadata);
                return;
            } catch (Exception ex) {
                logAndMaybeRetry(attempt, ex);
            }
        }
        throw new RuntimeException("Failed to upload media after max retries.");
    }

    @Override
    protected void deleteBlob(String blobName) {
        s3Client.deleteObject(storageProperties.getAwsBucketName(), blobName);
    }

    @Override
    public String generateSignedUrl(String objectName) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(storageProperties.getAwsBucketName(), objectName)
                .withMethod(HttpMethod.GET)
                .withExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000)); // 7 days expiration
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }
}
