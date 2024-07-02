package com.quashbugs.quash.service;

import com.quashbugs.quash.model.*;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    Pair<String, MediaType> upload(MultipartFile mediaFile, Report report) throws IOException;

    void deleteMedia(BugMedia bugMedia);

    String generateSignedUrl(String objectName);

    void deleteCrashLog(CrashLog crashLog);

    boolean deleteCloudMediaForChatUpload(ChatUploads chatUpload);

    void deleteGifBitmap(GifBitmap gifBitmap);
}
