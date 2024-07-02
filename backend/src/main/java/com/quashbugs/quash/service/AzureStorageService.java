package com.quashbugs.quash.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.quashbugs.quash.repo.*;
import com.quashbugs.quash.util.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;

import static com.quashbugs.quash.constants.Constants.MAX_RETRIES;

@Service
public class AzureStorageService extends AbstractStorageService {

    private final BlobServiceClient blobServiceClient;

    private final BlobContainerClient containerClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageService.class);

    @Autowired
    public AzureStorageService(BugMediaRepository bugMediaRepository,
                               GifMediaRepository gifMediaRepository,
                               CrashLogRepository crashLogRepository,
                               ApplicationRepository applicationRepository,
                               ChatUploadRepository chatUploadRepository,
                               StorageProperties storageProperties) {
        super(bugMediaRepository, gifMediaRepository, crashLogRepository, applicationRepository, chatUploadRepository, storageProperties);

        try {
            StorageSharedKeyCredential credential = new StorageSharedKeyCredential(storageProperties.getAzureAccountName(), storageProperties.getAzureAccountKey());
            this.blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint("https://" + storageProperties.getAzureAccountName() + ".blob.core.windows.net")
                    .credential(credential)
                    .buildClient();
            this.containerClient = blobServiceClient.getBlobContainerClient(storageProperties.getAzureContainerName());
        } catch (Exception e) {
            LOGGER.error("Error initializing AzureStorageService", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void uploadWithRetries(byte[] content, String objectName, String mimeType) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                BlobClient blobClient = containerClient.getBlobClient(objectName);
                blobClient.upload(new ByteArrayInputStream(content), content.length, true);
                blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(mimeType));
                return;
            } catch (Exception ex) {
                logAndMaybeRetry(attempt, ex);
            }
        }
        throw new RuntimeException("Failed to upload media after max retries.");
    }

    @Override
    protected void deleteBlob(String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.delete();
    }

    @Override
    public String generateSignedUrl(String objectName) {
        BlobClient blobClient = containerClient.getBlobClient(objectName);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(7), new BlobSasPermission().setReadPermission(true));
        String sasToken = blobClient.generateSas(sasValues);
        return blobClient.getBlobUrl() + "?" + sasToken;
    }
}
