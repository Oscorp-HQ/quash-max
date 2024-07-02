package com.quashbugs.quash.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.quashbugs.quash.repo.*;
import com.quashbugs.quash.util.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.quashbugs.quash.constants.Constants.MAX_RETRIES;

@Service
public class GcpStorageService extends AbstractStorageService {

    private final Storage storage;

    private static final Logger LOGGER = LoggerFactory.getLogger(GcpStorageService.class);

    @Autowired
    public GcpStorageService(BugMediaRepository bugMediaRepository,
                             GifMediaRepository gifMediaRepository,
                             CrashLogRepository crashLogRepository,
                             ApplicationRepository applicationRepository,
                             ChatUploadRepository chatUploadRepository,
                             StorageProperties storageProperties) {
        super(bugMediaRepository, gifMediaRepository, crashLogRepository, applicationRepository, chatUploadRepository, storageProperties);
        try {
            String clientEmail = storageProperties.getGcpClientEmail();
            String privateKey = storageProperties.getGcpPrivateKey();
            String projectId = storageProperties.getGcpProjectId();

            GoogleCredentials credentials = ServiceAccountCredentials.fromPkcs8(
                    null,  // clientId is not necessary
                    clientEmail,
                    privateKey,
                    null,  // privateKeyId is not necessary
                    null   // scopes are not necessary at this point
            );
            storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build()
                    .getService();
        } catch (Exception e) {
            LOGGER.error("Error initializing MediaStorageService", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void uploadWithRetries(byte[] content, String objectName, String mimeType) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                BlobInfo blobInfo = BlobInfo.newBuilder(storageProperties.getGcpBucketName(), objectName)
                        .setContentType(mimeType)
                        .build();
                storage.create(blobInfo, content);
                return;
            } catch (StorageException ex) {
                logAndMaybeRetry(attempt, ex);
            }
        }
        throw new RuntimeException("Failed to upload media after max retries.");
    }

    @Override
    protected void deleteBlob(String blobName) {
        BlobId blobId = BlobId.of(storageProperties.getGcpBucketName(), blobName);
        if (!storage.delete(blobId)) {
            throw new RuntimeException("Failed to delete blob: " + blobName);
        }
    }

    @Override
    public String generateSignedUrl(String objectName) {
        URL signedUrl = storage.signUrl(
                BlobInfo.newBuilder(storageProperties.getGcpBucketName(), objectName).build(),
                7,  // 1 hour expiration
                TimeUnit.DAYS,
                Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                Storage.SignUrlOption.withV4Signature()
        );
        return signedUrl.toString();
    }
}