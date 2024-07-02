package com.quashbugs.quash.service;

import com.quashbugs.quash.dto.miscellaneous.InMemoryMultipartDTO;
import com.quashbugs.quash.dto.miscellaneous.MetaDataDTO;
import com.quashbugs.quash.dto.miscellaneous.ThreadUploadsMediaDTO;
import com.quashbugs.quash.dto.request.IssuesRequestBodyDTO;
import com.quashbugs.quash.dto.request.ReportRequestDTO;
import com.quashbugs.quash.dto.response.ChatThreadResponseDTO;
import com.quashbugs.quash.dto.response.PaginatedResponseDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.exceptions.OrganisationNotFoundException;
import com.quashbugs.quash.exceptions.ReportNotFoundException;
import com.quashbugs.quash.model.*;
import com.quashbugs.quash.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.quashbugs.quash.constants.Constants.FIFTEEN_MB;

@Service
public class ReportsService {

    private final OrganisationRepository organisationRepository;

    private final DeviceMetaRepository deviceMetadataRepository;

    private final ReportRepository reportRepository;

    private final StorageService storageService;

    private final JwtService jwtService;

    private final GifCreationService gifCreationService;

    private final UtilsService utilsService;

    private final SlackIntegrationService slackService;

    private final NetworkRepository networkRepository;

    private final ChatUploadRepository chatUploadRepository;

    private final ChatThreadRepository chatThreadRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportsService.class);

    @Autowired
    public ReportsService(OrganisationRepository organisationRepository,
                          DeviceMetaRepository deviceMetadataRepository,
                          ReportRepository reportRepository,
                          StorageService storageService,
                          JwtService jwtService,
                          GifCreationService gifCreationService,
                          UtilsService utilsService,
                          SlackIntegrationService slackService,
                          NetworkRepository networkRepository,
                          ChatUploadRepository chatUploadRepository,
                          ChatThreadRepository chatThreadRepository) {
        this.organisationRepository = organisationRepository;
        this.deviceMetadataRepository = deviceMetadataRepository;
        this.reportRepository = reportRepository;
        this.storageService = storageService;
        this.jwtService = jwtService;
        this.gifCreationService = gifCreationService;
        this.utilsService = utilsService;
        this.slackService = slackService;
        this.networkRepository = networkRepository;
        this.chatUploadRepository = chatUploadRepository;
        this.chatThreadRepository = chatThreadRepository;
    }

    public Report save(Report report) {
        return reportRepository.save(report);
    }

    public PaginatedResponseDTO<Report> getReportsByAppId(String appId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Report> reportPage = reportRepository.findByAppId(appId, pageable);
        enrichReportsWithSignedUrls(reportPage.getContent());

        MetaDataDTO metaDataDTO = new MetaDataDTO(
                reportPage.getNumber(),
                reportPage.getTotalPages(),
                reportPage.getTotalElements(),
                reportPage.getSize()
        );
        return new PaginatedResponseDTO<>(reportPage.getContent(), metaDataDTO);
    }

    public void enrichReportsWithSignedUrls(List<Report> reports) {
        List<String> allMediaRefs = new ArrayList<>();
        for (Report report : reports) {
            collectMediaRefs(report, allMediaRefs);
        }
        Map<String, String> signedUrls = utilsService.generateSignedUrls(allMediaRefs);
        for (Report report : reports) {
            applySignedUrlsToReport(report, signedUrls);
        }
    }

    public Report enrichUrlsForReport(Report report) {
        List<String> mediaRefs = new ArrayList<>();
        collectMediaRefs(report, mediaRefs);
        Map<String, String> signedUrls = utilsService.generateSignedUrls(mediaRefs);
        return applySignedUrlsToReport(report, signedUrls);
    }

    private void collectMediaRefs(Report report, List<String> mediaRefs) {
        if (report.getListOfMedia() != null) {
            for (BugMedia media : report.getListOfMedia()) {
                mediaRefs.add(media.getMediaRef());
            }
        }
        if (report.getCrashLog() != null) {
            mediaRefs.add(report.getCrashLog().getMediaRef());
        }
    }

    public Report applySignedUrlsToReport(Report report, Map<String, String> signedUrls) {
        if (report.getListOfMedia() != null) {
            for (BugMedia media : report.getListOfMedia()) {
                String signedUrl = signedUrls.get(media.getMediaRef());
                if (signedUrl != null) {
                    media.setMediaUrl(signedUrl);
                }
            }
        }
        if (report.getCrashLog() != null) {
            String signedUrl = signedUrls.get(report.getCrashLog().getMediaRef());
            if (signedUrl != null) {
                report.getCrashLog().setLogUrl(signedUrl);
            }
        }
        return report;
    }

    public Optional<Report> findReportById(String reportId) {
        return reportRepository.findById(reportId);
    }

    public Report createBugReport(ReportRequestDTO bugReportRequestDTO, Organisation organisation) throws Exception {
        try {
            String orgId = String.valueOf(organisation.getId());
            if (utilsService.doesAppBelongToThisOrg(bugReportRequestDTO.getAppId(), orgId)) {
                Report report = utilsService.createReportFromRequest(bugReportRequestDTO, orgId);
                if (bugReportRequestDTO.getMediaFiles() != null) {
                    List<BugMedia> mediaList = utilsService.saveMediaForReport(bugReportRequestDTO.getMediaFiles(), report);
                    report.setListOfMedia(mediaList);
                }
                if (bugReportRequestDTO.getCrashLog() != null) {
                    CrashLog log = utilsService.saveCrashLog(bugReportRequestDTO.getCrashLog(), report);
                    report.setCrashLog(log);
                }
                if (bugReportRequestDTO.getDeviceMetadata() != null) {
                    DeviceMetadata deviceMetadata = getOrSaveDeviceMetadata(bugReportRequestDTO.getDeviceMetadata(), organisation);
                    report.setDeviceMetadata(deviceMetadata);
                }
                save(report);
                return report;
            } else {
                throw new Exception("App does not belong to this org");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional(rollbackFor = ReportNotFoundException.class)
    public List<NetworkLog> saveNetworkLogs(List<NetworkLog> networkLogs, String reportId) throws ReportNotFoundException {
        if (reportRepository.findById(reportId).isPresent()) {
            for (NetworkLog log : networkLogs) {
                log.setReportId(reportId);
            }
            networkRepository.saveAll(networkLogs);
            return networkLogs;
        } else {
            throw new ReportNotFoundException("Report with ID " + reportId + " doesn't exist.");
        }
    }

    public List<NetworkLog> getNetworkLogs(String reportId) {
        return networkRepository.findByReportId(reportId);
    }

    public boolean exportIssuesToSlack(IssuesRequestBodyDTO issues) throws Exception {
        for (var issue : issues.getIssues()) {
            var reportOpt = findReportById(issue);
            if (reportOpt.isPresent()) {
                var report = reportOpt.get();
                slackService.checkAndPublishNotificationOnSlack(report.getAppId(), report);
            } else {
                return false;
            }
        }
        return true;
    }

    public void deleteReport(String reportId) throws ReportNotFoundException, Exception {
        Optional<Report> existingReportOpt = reportRepository.findById(reportId);
        if (existingReportOpt.isPresent()) {
            Report report = existingReportOpt.get();
            deleteReportData(report);
            reportRepository.delete(report);
        } else {
            throw new ReportNotFoundException("Report not found");
        }
    }

    private void deleteReportData(Report report) throws Exception {
        if (report.getListOfMedia() != null) {
            report.getListOfMedia().forEach(storageService::deleteMedia);
        }
        if (report.getCrashLog() != null) {
            storageService.deleteCrashLog(report.getCrashLog());
        }
        deleteThreadForReportId(report.getId());

        deleteReportNetworkLogs(report.getId());
    }

    private void deleteReportNetworkLogs(String reportId) {
        networkRepository.deleteByReportId(reportId);
    }

    public void validateRequest(ReportRequestDTO request) {
        if (request.getCrashLog() != null && request.getCrashLog().getSize() > FIFTEEN_MB) {
            throw new IllegalArgumentException("Crash log size should not exceed 15 MB");
        }
        if (request.getMediaFiles() != null && request.getMediaFiles().stream().anyMatch(file -> file.getSize() > FIFTEEN_MB)) {
            throw new IllegalArgumentException("Media file size should not exceed 15 MB");
        }
    }

    public Organisation getOrganisationFromToken(String token) throws OrganisationNotFoundException {
        String orgId = jwtService.extractOrgIdFromToken(token.substring(7));
        return organisationRepository.findById(Long.valueOf(orgId))
                .orElseThrow(() -> new OrganisationNotFoundException("Organisation not found"));
    }

    public DeviceMetadata getOrSaveDeviceMetadata(DeviceMetadata metadata, Organisation organisation) {
        return deviceMetadataRepository.findByDeviceAndOsAndOrganisation(metadata.getDevice(), metadata.getOs(), organisation)
                .orElseGet(() -> {
                    metadata.setOrganisation(organisation);
                    deviceMetadataRepository.save(metadata);
                    return metadata;
                });
    }

    public void deleteAllReportsByAppId(String appId) throws ReportNotFoundException, Exception {
        var allReports = reportRepository.findByAppId(appId);
        for (Report report : allReports) {
            deleteReport(report.getId());
        }
    }

    public void saveChatUploads(ChatUploads uploads) {
        chatUploadRepository.save(uploads);
    }

    public ChatThread saveChatThread(ChatThread chatThread) {
        return chatThreadRepository.save(chatThread);
    }

    public List<ChatThreadResponseDTO> getThreadResponsesByReport(Report report) {
        List<ChatThread> threads = getThreadsByReport(report);
        List<ChatThreadResponseDTO> threadResponses = new ArrayList<>();

        for (ChatThread thread : threads) {
            ChatThreadResponseDTO threadResponse = new ChatThreadResponseDTO();
            threadResponse.setId(thread.getId());
            threadResponse.setPosterId(thread.getPosterId());
            threadResponse.setMessages(thread.getMessages());
            threadResponse.setMentions(thread.getMentions());
            threadResponse.setTimestamp(thread.getTimestamp());
            threadResponse.setUploads(getUploadsUrls(thread));
            threadResponses.add(threadResponse);
        }
        return threadResponses;
    }

    public List<ChatThread> getThreadsByReport(Report report) {
        return chatThreadRepository.findByReport(report);
    }

    public ArrayList<ThreadUploadsMediaDTO> getUploadsUrls(ChatThread thread) {
        List<ChatUploads> listOfUploads = chatUploadRepository.findByChatThread(thread);
        List<String> mediaRefs = listOfUploads.stream()
                .map(ChatUploads::getMediaRef)
                .toList();
        Map<String, String> signedUrls = utilsService.generateSignedUrls(mediaRefs);

        ArrayList<ThreadUploadsMediaDTO> uploadsUrls = new ArrayList<>();
        for (ChatUploads upload : listOfUploads) {
            String signedUrl = signedUrls.get(upload.getMediaRef());
            if (signedUrl != null) {
                var data = ThreadUploadsMediaDTO.builder().mediaType(upload.getMediaType()).url(signedUrl).build();
                uploadsUrls.add(data);
            } else {
                LOGGER.warn("Signed URL not found for mediaRef: {}", upload.getMediaRef());
            }
        }
        return uploadsUrls;
    }

    public boolean deleteThreadForReportId(String reportId) throws Exception {
        try {
            Optional<Report> reportOptional = findReportById(reportId);
            if (reportOptional.isPresent()) {
                Report report = reportOptional.get();
                List<ChatThread> listOfThreads = chatThreadRepository.findByReport(report);
                for (ChatThread chatThread : listOfThreads) {
                    List<ChatUploads> uploads = chatUploadRepository.findByChatThread(chatThread);
                    for (ChatUploads chatUpload : uploads) {
                        if (!storageService.deleteCloudMediaForChatUpload(chatUpload)) {
                            LOGGER.warn("Failed to delete media {} from cloud storage", chatUpload.getMediaRef());
                        }
                    }
                    chatThreadRepository.delete(chatThread);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Error deleting threads for report: {}", e.getMessage(), e);
            throw new Exception("Error deleting threads for report: " + e.getMessage());
        }
    }


    public boolean deleteThreadForThreadId(String threadId) throws Exception {
        try {
            Optional<ChatThread> threadOptional = chatThreadRepository.findById(threadId);
            if (threadOptional.isPresent()) {
                ChatThread thread = threadOptional.get();
                List<ChatUploads> uploads = chatUploadRepository.findByChatThread(thread);
                if (!uploads.isEmpty()) {
                    for (ChatUploads chatUpload : uploads) {
                        if (!storageService.deleteCloudMediaForChatUpload(chatUpload)) {
                            LOGGER.error("Failed to delete media {} from cloud storage", chatUpload.getMediaRef());
                        }
                    }
                }
                chatThreadRepository.delete(thread);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Error deleting thread: " + e.getMessage());
        }
    }

    public Report getReportById(String reportId) {
        var reportOpt = reportRepository.findById(reportId);
        var report = reportOpt.get();
        List<String> allMediaRef = new ArrayList<>();
        if (report.getListOfMedia() != null) {
            for (BugMedia media : report.getListOfMedia()) {
                allMediaRef.add(media.getMediaRef());
            }
        }
        if (report.getCrashLog() != null) {
            allMediaRef.add(report.getCrashLog().getMediaRef());
        }
        Map<String, String> signedUrls = utilsService.generateSignedUrls(allMediaRef);
        applySignedUrlsToReport(report, signedUrls);

        return report;
    }

    @Async
    public CompletableFuture<ResponseEntity<ResponseDTO>> processBitmapsAndCreateGif(Report report, List<MultipartFile> files) {
        try {
            updateReportStatus(report, GifStatus.PROCESSING);
            List<GifBitmap> gifBitmaps = utilsService.saveGifBitmapsForReport(files, report);
            LOGGER.info("Validating {} bitmaps for report ID: {}", files.size(), report.getId());
            for (MultipartFile file : files) {
                try {
                    BufferedImage image = ImageIO.read(file.getInputStream());
                    if (image == null) {
                        throw new IOException("Invalid image format: " + file.getOriginalFilename());
                    }
                } catch (IOException e) {
                    updateReportStatus(report, GifStatus.FAILED);
                    throw new RuntimeException("An error occurred while validating the input bitmaps: " + e.getMessage(), e);
                }
            }
            LOGGER.info("Creating GIF for report ID: {}", report.getId());
            try (ByteArrayOutputStream gifOutputStream = gifCreationService.createGif(files, 120)) {
                MultipartFile gifFile = new InMemoryMultipartDTO(gifOutputStream.toByteArray(), "generated.gif", "image/gif");
                Pair<String, MediaType> uploadResult = storageService.upload(gifFile, report);
                return CompletableFuture.completedFuture(handleUploadSuccess(report, gifBitmaps, uploadResult));
            }
        } catch (IOException e) {
            updateReportStatus(report, GifStatus.FAILED);
            throw new RuntimeException("An error occurred while processing the GIF generation request: " + e.getMessage(), e);
        } catch (Exception e) {
            updateReportStatus(report, GifStatus.FAILED);
            throw new RuntimeException(e);
        }
    }

    @Async
    public CompletableFuture<ResponseEntity<ResponseDTO>> startGifProcessing(Report report) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOGGER.info("Updating report status to PROCESSING for report ID: {}", report.getId());
                updateReportStatus(report, GifStatus.PROCESSING);

                List<GifBitmap> gifBitmaps = report.getListOfGif();
                LOGGER.info("Downloading media as byte arrays for report ID: {}", report.getId());
                List<byte[]> imageByteArrayList = utilsService.downloadMediaAsByteArrays(gifBitmaps);

                LOGGER.info("Creating GIF from byte arrays for report ID: {}", report.getId());
                try (ByteArrayOutputStream gifOutputStream = gifCreationService.createGifFromByteArrays(imageByteArrayList, 120)) {
                    MultipartFile gifFile = new InMemoryMultipartDTO(gifOutputStream.toByteArray(), "generated.gif", "image/gif");
                    LOGGER.info("Uploading generated GIF for report ID: {}", report.getId());
                    Pair<String, com.quashbugs.quash.model.MediaType> uploadResult = storageService.upload(gifFile, report);
                    return handleUploadSuccess(report, gifBitmaps, uploadResult);
                }
            } catch (Exception e) {
                LOGGER.error("An error occurred while processing the GIF generation request for report ID: {}", report.getId(), e);
                updateReportStatus(report, GifStatus.FAILED);
                throw new RuntimeException("An error occurred while processing the GIF generation request: " + e.getMessage(), e);
            }
        });
    }

    private ResponseEntity<ResponseDTO> handleUploadSuccess(Report report, List<GifBitmap> gifBitmaps, Pair<String, com.quashbugs.quash.model.MediaType> uploadResult) {
        if (uploadResult != null) {
            gifBitmaps.forEach(storageService::deleteGifBitmap);
            BugMedia bugMedia = utilsService.getMediaObject(uploadResult.getFirst());
            bugMedia.setMediaUrl(storageService.generateSignedUrl(uploadResult.getFirst()));
            if (report.getListOfMedia() == null) {
                ArrayList<BugMedia> bugMediaList = new ArrayList<>();
                bugMediaList.add(bugMedia);
                report.setListOfMedia(bugMediaList);
            } else {
                report.getListOfMedia().add(bugMedia);
            }
            updateReportStatus(report, GifStatus.COMPLETED);
            return ResponseEntity.ok(new ResponseDTO(true, "GIF successfully created and uploaded.", bugMedia));
        } else {
            updateReportStatus(report, GifStatus.FAILED);
            return ResponseEntity.internalServerError().body(new ResponseDTO(false, "Failed to upload the GIF after creation.", null));
        }
    }

    private void updateReportStatus(Report report, GifStatus status) {
        report.setGifStatus(status);
        save(report);
    }
}