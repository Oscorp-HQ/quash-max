package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.miscellaneous.ThreadUploadsMediaDTO;
import com.quashbugs.quash.dto.request.NetworkLogRequestBodyDTO;
import com.quashbugs.quash.dto.request.PostThreadRequestBodyDTO;
import com.quashbugs.quash.dto.request.ReportRequestDTO;
import com.quashbugs.quash.dto.request.UpdateReportRequestDTO;
import com.quashbugs.quash.dto.response.ChatThreadResponseDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.exceptions.ReportNotFoundException;
import com.quashbugs.quash.model.*;
import com.quashbugs.quash.service.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/report")
@SecurityRequirement(name = "jwtAuth")
public class ReportsController {

    private final StorageService storageService;

    private final ReportsService reportService;

    private final UtilsService utilsService;

    private final EmailService emailService;

    private final ApplicationService applicationService;

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    @Autowired
    public ReportsController(StorageService storageService,
                             ReportsService reportService,
                             UtilsService utilsService,
                             EmailService emailService,
                             ApplicationService applicationService) {
        this.storageService = storageService;
        this.reportService = reportService;
        this.utilsService = utilsService;
        this.emailService = emailService;
        this.applicationService = applicationService;
    }

    /**
     * Deletes a bug report and its associated data.
     *
     * @param reportId The ID of the report to be deleted.
     * @return ResponseEntity with a ResponseDTO indicating the success of the deletion.
     */
    @DeleteMapping("/{reportId}")
    public ResponseEntity<ResponseDTO> deleteReport(@PathVariable String reportId) {
        try {
            reportService.deleteReport(reportId);
            return ResponseEntity.ok(new ResponseDTO(true, "Report and associated data deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "Error while deleting report: " + e.getMessage(), null));
        } catch (ReportNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(false, "Report not found", null));
        }
    }

    /**
     * Updates a bug report with new information.
     *
     * @param reportId      The ID of the report to be updated.
     * @param updateRequest The request containing updates for the bug report.
     * @return ResponseEntity with a ResponseDTO indicating the success of the update.
     */
    @PatchMapping(value = "/{reportId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<ResponseDTO> updateBugReport(@PathVariable String reportId,
                                                       @ModelAttribute UpdateReportRequestDTO updateRequest
    ) {
        try {
            Optional<Report> existingReport = reportService.findReportById(reportId);
            if (existingReport.isPresent()) {
                Report existingUpdatedReport = utilsService.applyUpdatesToReport(existingReport.get(), updateRequest);
                var report = reportService.save(existingUpdatedReport);
                var response = reportService.enrichUrlsForReport(report);
                return ResponseEntity.ok(new ResponseDTO(true, "Bug report updated successfully", response));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "Couldn't save bug", null));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error while updating bug report: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "Couldn't update bug report", e.getMessage()));
        }
    }

    /**
     * Creates a new bug report.
     *
     * @param bugReportRequestDTO The request containing details for creating the bug report.
     * @return ResponseEntity with a ResponseDTO indicating the success of the bug report creation.
     */
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<ResponseDTO> createBugReport(@ModelAttribute ReportRequestDTO bugReportRequestDTO, Authentication authentication) {
        try {
            Organisation organisation = applicationService.getOrganisationFromObject(authentication.getPrincipal());
            if (organisation != null) {
                reportService.validateRequest(bugReportRequestDTO);
                Report report = reportService.createBugReport(bugReportRequestDTO, organisation);
                return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO(true, "Bug Successfully Reported", report));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(false, "Organisation not found", null));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error while creating bug report: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "Couldn't save bug", e.getMessage()));
        }
    }

    /**
     * Receives a list of bitmap files and creates a GIF from them.
     *
     * @param reportId The ID of the report.
     * @param files    The list of bitmap files.
     * @return A CompletableFuture that represents the asynchronous result of creating the GIF from the bitmaps.
     */
    @PostMapping("/{reportId}/bitmaps")
    public CompletableFuture<ResponseEntity<ResponseDTO>> receiveBitmapsAndCreateGif(
            @PathVariable String reportId,
            @RequestParam("bitmaps") List<MultipartFile> files) {
        logger.info("Processing bitmaps for report ID: {}", reportId);
        if (reportId == null || reportId.isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(false, "Invalid Report ID", null)));
        }
        CompletableFuture<Report> reportFuture = reportService.findReportById(reportId)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> CompletableFuture.failedFuture(new ReportNotFoundException("Report not found with ID: " + reportId)));

        return reportFuture.thenCompose(report -> {
            logger.info("Processing bitmaps for report ID: {}", reportId);
            return reportService.processBitmapsAndCreateGif(report, files);
        }).exceptionally(this::handleExceptions);
    }

    private ResponseEntity<ResponseDTO> handleExceptions(Throwable e) {
        if (e instanceof ReportNotFoundException) {
            return ResponseEntity.notFound().build();
        } else if (e instanceof RuntimeException && e.getCause() != null) {
            return ResponseEntity.internalServerError().body(new ResponseDTO(false, e.getCause().getMessage(), null));
        } else {
            return ResponseEntity.internalServerError().body(new ResponseDTO(false, "An unexpected error occurred.", null));
        }
    }

    /**
     * Generates a GIF from a report.
     *
     * @param reportId The ID of the report for which the GIF needs to be generated.
     * @return A response entity containing the status and message indicating the result of the GIF generation process.
     */
    @GetMapping("/{reportId}/generate-gif")
    @Transactional
    public ResponseEntity<ResponseDTO> generateGif(@PathVariable String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseDTO(false, "The provided report ID is invalid or missing.", null));
        }
        Report report;
        try {
            report = reportService.findReportById(reportId).orElseThrow(() ->
                    new ReportNotFoundException("No report found with the specified ID: " + reportId));
        } catch (ReportNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        if (report.getGifStatus() == GifStatus.COMPLETED) {
            return ResponseEntity.ok(new ResponseDTO(true, "Media has already been processed for this report.", null));
        }
        logger.info("Starting GIF processing for report ID: {}", reportId);
        CompletableFuture<ResponseEntity<ResponseDTO>> futureResponse = reportService.startGifProcessing(report);
        try {
            return futureResponse.get(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("The request was interrupted for report ID: {}", reportId, e);
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(new ResponseDTO(false, "The request was interrupted.", null));
        } catch (ExecutionException e) {
            logger.error("An error occurred while processing the GIF generation request for report ID: {}", reportId, e);
            Throwable cause = e.getCause();
            return ResponseEntity.internalServerError().body(new ResponseDTO(false,
                    "An error occurred while processing the GIF generation request: " + cause.getMessage(), null));
        } catch (TimeoutException e) {
            logger.error("Processing timeout exceeded for report ID: {}", reportId, e);
            return ResponseEntity.internalServerError().body(new ResponseDTO(false, "Processing timeout exceeded.", null));
        }
    }

    /**
     * Saves network logs associated with a specific bug report.
     *
     * @param reportId    The ID of the bug report for which the network logs are being saved.
     * @param networkLogs The request body containing the network logs to be saved.
     * @return A response entity containing the result of the operation, including a success or failure message and the saved network logs.
     */
    @PostMapping("/{reportId}/network-logs")
    public ResponseEntity<ResponseDTO> saveNetworkLogs(@PathVariable String reportId, @RequestBody NetworkLogRequestBodyDTO networkLogs) {
        try {
            if (reportId == null || reportId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(false, "Invalid Report ID", null));
            }
            List<NetworkLog> logs = reportService.saveNetworkLogs(networkLogs.getNetworkLogs(), reportId);
            return ResponseEntity.ok(new ResponseDTO(true, "Network logs saved successfully", logs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "An error occurred while saving network logs", e.getMessage()));
        } catch (ReportNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "Report not found" + e.getMessage(), e.getMessage()));
        }
    }

    /**
     * Retrieves network logs for a specific report by its ID.
     *
     * @param reportId The ID of the report for which to retrieve network logs.
     * @return The response containing the network logs or an error message.
     */
    @GetMapping("/network-logs/{reportId}")
    public ResponseEntity<ResponseDTO> getNetworkLogsForReport(@PathVariable String reportId) {
        try {
            if (reportId == null || reportId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(false, "Invalid Report ID", null));
            }
            List<NetworkLog> logs = reportService.getNetworkLogs(reportId);
            if (logs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(false, "No network logs found for this report ID", null));
            }
            return ResponseEntity.ok(new ResponseDTO(true, "Network logs fetched successfully", logs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(false, "An error occurred while fetching network logs", e.getMessage()));
        }
    }

    /**
     * Retrieves bug reports for a specific application.
     *
     * @param appId The ID of the application for which to retrieve bug reports.
     * @param page  The page number for paginated results.
     * @param size  The number of bug reports to retrieve per page.
     * @return ResponseEntity with a ResponseDTO containing the retrieved bug reports.
     */
    @GetMapping("")
    @SecurityRequirement(name = "jwtAuth")
    public ResponseEntity<ResponseDTO> getReports(@RequestParam String appId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  Authentication authentication) {
        try {
            Organisation organisation = applicationService.getOrganisationFromObject(authentication.getPrincipal());
            if (organisation != null) {
                String orgId = String.valueOf(organisation.getId());
                if (utilsService.doesAppBelongToThisOrg(appId, orgId)) {
                    var response = reportService.getReportsByAppId(appId, page, size);
                    return new ResponseEntity<>(new ResponseDTO(true, "Bugs fetched successfully", response), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ResponseDTO(false, "Access denied or application does not exist.", null), HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not found", null), HttpStatus.UNAUTHORIZED);
            }
        } catch (NullPointerException ex) {
            return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not found", null), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a report by its ID.
     *
     * @param reportId The ID of the report to be fetched.
     * @return ResponseEntity<ResponseDTO> The response entity containing the fetched report or an error message.
     */
    @GetMapping(value = "/getReportById")
    public ResponseEntity<ResponseDTO> getReportByReportId(@RequestParam String reportId) {
        try {
            var report = reportService.getReportById(reportId);
            if (report != null) {
                return new ResponseEntity<>(new ResponseDTO(true, "Successfully fetched the report", report), HttpStatus.OK);
            }
            return new ResponseEntity<>(new ResponseDTO(false, "Report doesn't exist", null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a new chat thread for a specific bug report.
     * Saves the thread to the database, uploads any attachments associated with the thread,
     * and sends email notifications if there are any mentions in the thread.
     *
     * @param postThreadRequestBodyDTO An object containing the necessary information to create a new chat thread.
     *                              It includes the report ID, poster ID, messages, mentions, and attachments.
     * @return ResponseEntity<ResponseDTO> A response object containing the status of the operation and the saved ChatThread object.
     */
    @PostMapping(value = "/comment-thread", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> postThread(@ModelAttribute PostThreadRequestBodyDTO postThreadRequestBodyDTO) {
        try {
            var currentReport = reportService.findReportById(postThreadRequestBodyDTO.getReportId());
            if (currentReport.isPresent()) {
                ChatThread chatThread = ChatThread.builder()
                        .posterId(postThreadRequestBodyDTO.getPosterId())
                        .messages(postThreadRequestBodyDTO.getMessages())
                        .mentions(postThreadRequestBodyDTO.getMentions())
                        .timestamp(Instant.now().toString())
                        .report(currentReport.get())
                        .build();
                var savedChatThread = reportService.saveChatThread(chatThread);

                if (postThreadRequestBodyDTO.getAttachments() != null && !postThreadRequestBodyDTO.getAttachments().isEmpty()) {
                    uploadAndRetrieveList(savedChatThread, postThreadRequestBodyDTO.getAttachments(), currentReport.get());
                }
                ArrayList<ThreadUploadsMediaDTO> mediaDTO = reportService.getUploadsUrls(savedChatThread);
                if (postThreadRequestBodyDTO.getMentions() != null) {
                    emailService.sendMentionNotification(postThreadRequestBodyDTO, savedChatThread.getTimestamp(), mediaDTO);
                }
                return new ResponseEntity<>(new ResponseDTO(true, "Thread added successfully.", savedChatThread), HttpStatus.OK);
            }
            return new ResponseEntity<>(new ResponseDTO(false, "Current Report is Empty", null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void uploadAndRetrieveList(ChatThread chatThread, ArrayList<MultipartFile> attachments, Report currentReport) throws IOException {
        for (MultipartFile attachment : attachments) {
            var data = storageService.upload(attachment, currentReport);
            var chatUpload = ChatUploads.builder().mediaRef(data.getFirst()).mediaType(data.getSecond().toString()).chatThread(chatThread).build();
            reportService.saveChatUploads(chatUpload);
        }
    }

    /**
     * Retrieves the chat threads associated with a specific bug report.
     *
     * @param reportId The ID of the bug report for which to retrieve the chat threads.
     * @return A response entity containing the status of the operation and the retrieved chat threads (if any).
     */
    @GetMapping("get-thread")
    public ResponseEntity<ResponseDTO> getThreadsForReport(@RequestParam String reportId) {
        try {
            var currentReport = reportService.findReportById(reportId);
            if (currentReport.isPresent()) {
                List<ChatThreadResponseDTO> threads = reportService.getThreadResponsesByReport(currentReport.get());
                if (!threads.isEmpty()) {
                    return new ResponseEntity<>(new ResponseDTO(true, "Threads retrieved successfully.", threads), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ResponseDTO(false, "No threads found for report ID: " + reportId, null), HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(new ResponseDTO(false, "Report not found for ID: " + reportId, null), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes the thread associated with a specific report.
     *
     * @param authentication the authentication object representing the user making the request
     * @param reportId       the ID of the report for which the thread needs to be deleted
     * @return a ResponseEntity containing a ResponseDTO indicating whether the thread was successfully deleted or not
     */
    @DeleteMapping("delete-all-threads")
    public ResponseEntity<ResponseDTO> deleteThreadWithReportId(Authentication authentication, @RequestParam String reportId) {
        try {
            User user = (User) authentication.getPrincipal();
            var isThreadDeleted = reportService.deleteThreadForReportId(reportId);
            return new ResponseEntity<>(new ResponseDTO(true, "Thread is deleted successfully.", isThreadDeleted), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(false, "Error while deleting thred: " + e.getMessage(), null));
        }
    }

    /**
     * Deletes a thread with the given thread ID.
     *
     * @param threadId The ID of the thread to be deleted.
     * @return A response entity indicating the status of the deletion operation, a message indicating the result, and the deleted thread data (if successful).
     */
    @DeleteMapping("delete-thread")
    public ResponseEntity<ResponseDTO> deleteReportWithThreadId(@RequestParam String threadId) {
        try {
            var data = reportService.deleteThreadForThreadId(threadId);
            return new ResponseEntity<>(new ResponseDTO(true, "Thread is deleted successfully.", data), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(false, "Error while deleting thred: " + e.getMessage(), null));
        }
    }
}