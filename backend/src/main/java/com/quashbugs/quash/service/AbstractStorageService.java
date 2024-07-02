package com.quashbugs.quash.service;

import com.quashbugs.quash.model.*;
import com.quashbugs.quash.repo.*;
import com.quashbugs.quash.util.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static com.quashbugs.quash.constants.Constants.*;

public abstract class AbstractStorageService implements StorageService {

    protected final BugMediaRepository bugMediaRepository;

    protected final GifMediaRepository gifMediaRepository;

    protected final CrashLogRepository crashLogRepository;

    protected final ApplicationRepository applicationRepository;

    protected final ChatUploadRepository chatUploadRepository;

    protected final StorageProperties storageProperties;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageService.class);

    protected AbstractStorageService(BugMediaRepository bugMediaRepository,
                                     GifMediaRepository gifMediaRepository,
                                     CrashLogRepository crashLogRepository,
                                     ApplicationRepository applicationRepository,
                                     ChatUploadRepository chatUploadRepository, StorageProperties storageProperties) {
        this.bugMediaRepository = bugMediaRepository;
        this.gifMediaRepository = gifMediaRepository;
        this.crashLogRepository = crashLogRepository;
        this.applicationRepository = applicationRepository;
        this.chatUploadRepository = chatUploadRepository;
        this.storageProperties = storageProperties;
    }

    @Override
    public Pair<String, MediaType> upload(MultipartFile mediaFile, Report report) throws IOException {
        String mimeType = mediaFile.getContentType();
        MediaType mediaType = determineMediaType(mimeType);

        byte[] content = extractBytesFromMedia(mediaFile);
        String objectName = generateUniqueObjectName(report, mediaType, mediaFile.getOriginalFilename(), mimeType);

        uploadWithRetries(content, objectName, mimeType);
        return Pair.of(objectName, mediaType);
    }

    @Override
    public void deleteMedia(BugMedia bugMedia) {
        try {
            deleteBlob(bugMedia.getMediaRef());
            bugMediaRepository.delete(bugMedia);
        } catch (Exception e) {
            LOGGER.error("Error deleting media: ", e);
        }
    }

    @Override
    public void deleteCrashLog(CrashLog crashLog) {
        try {
            deleteBlob(crashLog.getMediaRef());
            crashLogRepository.delete(crashLog);
        } catch (Exception e) {
            LOGGER.error("Error deleting crash logs: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteCloudMediaForChatUpload(ChatUploads chatUpload) {
        try {
            deleteBlob(chatUpload.getMediaRef());
            chatUploadRepository.delete(chatUpload);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error deleting media: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void deleteGifBitmap(GifBitmap gifBitmap) {
        try {
            deleteBlob(gifBitmap.getMediaRef());
            gifMediaRepository.delete(gifBitmap);
        } catch (Exception e) {
            LOGGER.error("Error deleting GifBitmap: {}", e.getMessage(), e);
        }
    }

    protected abstract void uploadWithRetries(byte[] content, String objectName, String mimeType);

    protected abstract void deleteBlob(String blobName);

    protected MediaType determineMediaType(String mimeType) throws IOException {
        if (ALLOWED_IMAGE_MIME_TYPES.contains(mimeType)) {
            return MediaType.IMAGE;
        } else if (ALLOWED_VIDEO_MIME_TYPES.contains(mimeType)) {
            return MediaType.VIDEO;
        } else if (ALLOWED_TXT_MIME_TYPE.equals(mimeType)) {
            return MediaType.CRASH;
        } else if (ALLOWED_AUDIO_MIME_TYPES.contains(mimeType)) {
            return MediaType.AUDIO;
        } else if (ALLOWED_PDF_MIME_TYPES.contains(mimeType)) {
            return MediaType.PDF;
        } else if (ALLOWED_GIF_MIME_TYPES.contains(mimeType)) {
            return MediaType.GIF;
        } else {
            throw new IOException("Unsupported file type: " + mimeType);
        }
    }

    protected byte[] extractBytesFromMedia(MultipartFile mediaFile) throws IOException {
        return mediaFile.getBytes();
    }

    protected String generateUniqueObjectName(Report report, MediaType mediaType, String originalFilename, String mimeType) {
        String basePath = determineOrganization(report);
        if (mediaType == MediaType.CRASH) {
            basePath += "/crashlogs";
        } else {
            basePath += "/media";
        }
        return basePath + "/" + UUID.randomUUID() + getFileExtension(originalFilename, mimeType);
    }

    protected String determineOrganization(Report report) {
        var appId = report.getAppId();
        var app = applicationRepository.findById(appId);
        if (app.isPresent()) {
            var organisation = app.get().getOrganisation();
            if (organisation != null) {
                return organisation.getName() + "/" + app.get().getAppName();
            } else return null;
        } else {
            return null;
        }
    }

    protected static String getFileExtension(String filename, String mimeType) {
        if ("audio/mpeg".equals(mimeType)) {
            return ".mp3";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            return filename.substring(dotIndex);
        }
        return "";
    }

    protected void logAndMaybeRetry(int attempt, Exception ex) {
        if (attempt < MAX_RETRIES) {
            LOGGER.warn(String.format("Attempt %d to upload media failed. Retrying...", attempt), ex);
            try {
                Thread.sleep(1000L * attempt);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } else {
            LOGGER.error(String.format("Attempt %d to upload media failed. No more retries.", attempt), ex);
        }
    }
}
