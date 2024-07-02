package com.quashbugs.quash.service;

import com.quashbugs.quash.controller.ReportsController;
import com.quashbugs.quash.dto.request.ReportRequestDTO;
import com.quashbugs.quash.dto.request.UpdateReportRequestDTO;
import com.quashbugs.quash.model.*;
import com.quashbugs.quash.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class UtilsService {

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    private final StorageService storageService;

    private final CrashStorageService crashStorageService;

    private final UserService userService;

    private final TeamMemberService teamMemberService;

    private final ApplicationRepository applicationRepository;

    private final ReportRepository reportRepository;

    private final OrganisationRepository organisationRepository;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final BugMediaRepository bugMediaRepository;

    private final GifMediaRepository gifMediaRepository;

    @Autowired
    public UtilsService(
            StorageService storageService,
            CrashStorageService crashStorageService,
            UserService userService,
            TeamMemberService teamMemberService,
            ApplicationRepository applicationRepository,
            ReportRepository reportRepository,
            OrganisationRepository organisationRepository,
            SequenceGeneratorService sequenceGeneratorService, BugMediaRepository bugMediaRepository, GifMediaRepository gifMediaRepository) {
        this.storageService = storageService;
        this.crashStorageService = crashStorageService;
        this.userService = userService;
        this.teamMemberService = teamMemberService;
        this.applicationRepository = applicationRepository;
        this.reportRepository = reportRepository;
        this.organisationRepository = organisationRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.bugMediaRepository = bugMediaRepository;
        this.gifMediaRepository = gifMediaRepository;
    }

    public Report applyUpdatesToReport(Report existingReport, UpdateReportRequestDTO updateRequest) throws Exception {

        // Save new media files if provided in the update request
        if (updateRequest.getNewMediaFiles() != null && !updateRequest.getNewMediaFiles().isEmpty()) {
            List<BugMedia> newMediaList = saveMediaForReport(updateRequest.getNewMediaFiles(), existingReport);
            if (existingReport.getListOfMedia() != null && !existingReport.getListOfMedia().isEmpty()) {
                existingReport.getListOfMedia().addAll(newMediaList);
            } else existingReport.setListOfMedia(newMediaList);
        }

        // Remove media items specified in the update request
        if (updateRequest.getMediaToRemoveIds() != null && !updateRequest.getMediaToRemoveIds().isEmpty()) {
            for (String mediaId : updateRequest.getMediaToRemoveIds()) {
                BugMedia mediaItem = existingReport.getListOfMedia().stream()
                        .filter(item -> item.getId().equals(mediaId))
                        .findFirst()
                        .orElse(null);

                // Delete media from storage and remove it from the report
                if (mediaItem != null) {
                    if (mediaItem.getMediaType().equals(MediaType.GIF)) {
                        existingReport.setGifStatus(GifStatus.DELETED);
                    }
                    storageService.deleteMedia(mediaItem);
                    existingReport.getListOfMedia().remove(mediaItem);
                }
            }
        }
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isEmpty()) {
            existingReport.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getDescription() != null && !updateRequest.getDescription().isEmpty()) {
            existingReport.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getStatus() != null && !updateRequest.getStatus().isEmpty()) {
            existingReport.setStatus(updateRequest.getStatus());
        }
        if (updateRequest.getType() != null && !updateRequest.getType().isEmpty()) {
            existingReport.setType(updateRequest.getType());
        }
        if (updateRequest.getPriority() != null && !updateRequest.getPriority().isEmpty()) {
            existingReport.setPriority(updateRequest.getPriority());
        }
        reportRepository.save(existingReport);
        return existingReport;
    }

    public boolean hasPermissionToUpdate(String reportId, String orgId) {
        try {
            var user = userService.findUserById(reportId);
            return user.filter(value -> doesUserBelongToSameOrganisation(value, orgId)).isPresent();
        } catch (Exception e) {
            return false;
        }
    }


    public boolean doesAppBelongToThisOrg(String id, String orgId) {
        Optional<QuashClientApplication> app = applicationRepository.findById(id);
        if (app.isPresent()) {
            if (String.valueOf(app.get().getOrganisation().getId()).equals(orgId)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public Report createReportFromRequest(ReportRequestDTO request, String orgId) {
        Optional<User> teamMember = userService.findUserById(request.getReporterId());

        Long orgIdValue = Long.valueOf(orgId); // Convert your orgId to Long type if it's in a different format.
        // Ensure the organization exists, otherwise throw an exception
        Organisation organisation = organisationRepository.findById(orgIdValue)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found!"));

        String organisationAbbreviation = organisation.getOrgAbbreviation();
        int seqNumber = sequenceGeneratorService.getNextSequenceNumber(orgId);
        String reportId = organisationAbbreviation + "-" + String.format("%03d", seqNumber);

        Report report = new Report();
        report.setId(reportId);
        report.setCreatedAt(new Date());
        report.setSource(request.getSource());
        report.setType(request.getType());
        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setPriority(request.getPriority());
        report.setStatus("OPEN");
        teamMember.ifPresent(report::setReportedBy);
        report.setAppId(request.getAppId());
        return report;
    }

    public List<BugMedia> saveMediaForReport(List<MultipartFile> mediaFiles, Report report) throws Exception {
        List<BugMedia> mediaList = new ArrayList<>();
        for (MultipartFile mediaFile : mediaFiles) {
            try {
                Pair<String, MediaType> result = storageService.upload(mediaFile, report);
                BugMedia media = new BugMedia();
                media.setMediaRef(result.getFirst());
                media.setMediaType(result.getSecond());
                media.setCreatedAt(new Date());
                mediaList.add(media);
            } catch (IOException e) {
                logger.error("Error uploading media file: ", e);
                throw new Exception("Error uploading media file: " + e.getMessage());
            }
        }
        return saveAll(mediaList);
    }

    public List<GifBitmap> saveGifBitmapsForReport(List<MultipartFile> gifBitmaps, Report report) throws Exception {
        List<GifBitmap> bitmapsList = new ArrayList<>();
        for (MultipartFile bitmap : gifBitmaps) {
            try {
                Pair<String, MediaType> result = storageService.upload(bitmap, report);
                GifBitmap gifBitmap = GifBitmap.builder()
                        .mediaRef(result.getFirst())
                        .mediaType(result.getSecond())
                        .createdAt(new Date())
                        .build();

                bitmapsList.add(gifBitmap);
            } catch (IOException e) {
                logger.error("Error uploading gif bitmaps: ", e);
                throw new Exception("Error uploading gif bitmaps: " + e.getMessage(), e);
            }
        }
        saveAllGifBitmaps(bitmapsList);
        report.setListOfGif(bitmapsList);
        reportRepository.save(report);
        return bitmapsList;
    }

    public List<byte[]> downloadMediaAsByteArrays(List<GifBitmap> gifBitmaps) throws IOException {
        List<byte[]> filesContent = new ArrayList<>();

        for (GifBitmap gifBitmap : gifBitmaps) {
            try {
                String mediaUrl = storageService.generateSignedUrl(gifBitmap.getMediaRef());
                byte[] fileContent = downloadMedia(mediaUrl);
                filesContent.add(fileContent);
            } catch (IOException e) {
                throw new IOException("Failed to download media: " + gifBitmap.getMediaRef(), e);
            }
        }

        return filesContent;
    }

    private byte[] downloadMedia(String mediaUrl) throws IOException {
        URL url = new URL(mediaUrl);
        try (InputStream in = url.openStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }

    public BugMedia getMediaObject(String mediaRef) {
        BugMedia bugMedia = new BugMedia();
        bugMedia.setMediaRef(mediaRef);
        bugMedia.setMediaType(MediaType.GIF);
        bugMedia.setCreatedAt(new Date());
        save(bugMedia);
        return bugMedia;
    }

    public CrashLog saveCrashLog(MultipartFile crashLog, Report report) {
        CrashLog log = new CrashLog();
        try {
            Pair<String, MediaType> result = storageService.upload(crashLog, report);
            log.setCreatedAt(new Date());
            log.setMediaRef(result.getFirst());
        } catch (Exception e) {
            logger.error("Error uploading media file: ", e);
        }
        return crashStorageService.saveCrashlog(log);
    }

    public boolean doesUserBelongToSameOrganisation(User user, String orgKey) {
        try {
            TeamMember teamMember = teamMemberService.findTeamMemberByOrganisation(user);
            if (teamMember.getOrganisation().getOrgUniqueKey().equals(orgKey)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, String> generateSignedUrls(List<String> mediaRefs) {
        List<CompletableFuture<Pair<String, String>>> futures = new ArrayList<>();
        for (String mediaRef : mediaRefs) {
            futures.add(
                    CompletableFuture.supplyAsync(() -> {
                        String signedUrl = storageService.generateSignedUrl(mediaRef);
                        return Pair.of(mediaRef, signedUrl);
                    })
            );
        }
        List<Pair<String, String>> results = futures.stream()
                .map(CompletableFuture::join).toList();

        return results.stream()
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    public String extractDomain(String email) {
        return email.substring(email.indexOf('@') + 1);
    }


    public List<BugMedia> saveAll(List<BugMedia> bugMedias) {
        return bugMediaRepository.saveAll(bugMedias);
    }

    public void saveAllGifBitmaps(List<GifBitmap> gifBitmaps) {
        gifMediaRepository.saveAll(gifBitmaps);
    }

    public BugMedia save(BugMedia bugMedia) {
        return bugMediaRepository.save(bugMedia);
    }

}