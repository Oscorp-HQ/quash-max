package com.quashbugs.quash.service;

import com.quashbugs.quash.dto.miscellaneous.ThreadUploadsMediaDTO;
import com.quashbugs.quash.dto.request.PostThreadRequestBodyDTO;
import com.quashbugs.quash.model.Report;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.ReportRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

@Service
public class EmailService {

    @Value("${from.email.address}")
    private String fromEmail;

    @Value("${spring.frontend.url}")
    private String frontendBaseUrl;

    private final JavaMailSender mailSender;

    private final UserService userService;

    private final ReportRepository reportRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender, UserService userService, ReportRepository reportRepository) {
        this.mailSender = mailSender;
        this.userService = userService;
        this.reportRepository = reportRepository;
    }

    public void sendVerificationEmail(User user, String verificationUrl) throws IOException {
        String emailTemplate = loadEmailTemplate("email-verification.html");
        String preparedHtml = StringUtils.replace(emailTemplate, "${verificationUrl}", verificationUrl);
        String finalPreparedHtml = StringUtils.replace(preparedHtml, "${emailAddress}", user.getWorkEmail());

        String subject = "Quash - Account Verification";
        String fromMessage = "Quash - Verify Your Email Address";
        sendMail(user, subject, fromMessage, finalPreparedHtml);
    }

    public void sendMail(User user, String subject, String fromMessage, String finalPreparedHtml) {
        MimeMessagePreparator preparator = mimeMessage -> {
            try {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                message.setTo(user.getWorkEmail());
                message.setFrom(fromEmail, fromMessage);
                message.setSubject(subject);
                message.setText(finalPreparedHtml, true);
            } catch (MessagingException e) {
                throw new RuntimeException("Error while setting up email", e);
            }
        };
        mailSender.send(preparator);
    }

    private String loadEmailTemplate(String fileName) throws IOException {
        Resource resource = new ClassPathResource("templates/" + fileName);

        try (InputStream inputStream = resource.getInputStream();
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    public void sendResetPasswordEmail(User user, String resetPasswordUrl) throws IOException {
        String emailTemplate = loadEmailTemplate("reset-password.html");
        String preparedHtml = StringUtils.replace(emailTemplate, "${verificationUrl}", resetPasswordUrl);
        String finalPreparedHtml = StringUtils.replace(preparedHtml, "${emailAddress}", user.getWorkEmail());

        String subject = "Reset Password";
        String fromMessage = "Quash - Reset Your Password";
        sendMail(user, subject, fromMessage, finalPreparedHtml);
    }

    public void sendInviteEmail(User user, String resetPasswordUrl, String ownerName, String ownerOrg) throws IOException {
        String emailTemplate = loadEmailTemplate("invite-email.html");

        String preparedHtml = emailTemplate.replace("${verificationUrl}", resetPasswordUrl);
        preparedHtml = preparedHtml.replace("${emailAddress}", user.getWorkEmail());
        preparedHtml = preparedHtml.replace("${personName}", ownerName);
        String finalPreparedHtml = preparedHtml.replace("${teamName}", ownerOrg);

        String subject = "Quash - Invitation";
        String fromMessage = "You are invited to catch bugs.";
        sendMail(user, subject, fromMessage, finalPreparedHtml);
    }

    public void sendWelcomeEmail(User user) throws IOException {
        String emailTemplate = loadEmailTemplate("welcome-email.html");

        String preparedHtml = emailTemplate.replace("${emailAddress}", user.getWorkEmail());
        preparedHtml = preparedHtml.replace("${frontend_base_url}", frontendBaseUrl);

        String subject = "Welcome to Quash! Let's set up your workspace";
        String fromMessage = "Team Quash";
        sendMail(user, subject, fromMessage, preparedHtml);
    }

    public void sendMentionNotification(PostThreadRequestBodyDTO postThreadRequestBodyDTO, String timestamp, ArrayList<ThreadUploadsMediaDTO> mediaDTOS) throws Exception {
        if (postThreadRequestBodyDTO.getMentions().isEmpty()) {
            return;
        }

        User posterUser = userService.findUserById(postThreadRequestBodyDTO.getPosterId())
                .orElseThrow(() -> new Exception("Poster user not found"));

        Report report = reportRepository.findById(postThreadRequestBodyDTO.getReportId())
                .orElseThrow(() -> new Exception("Report not found"));

        String threadRedirectUrl = frontendBaseUrl + "/dashboard?ticket=";

        String emailTemplate = loadEmailTemplate("mention-notification.html");

        StringBuilder ticketRedirectUrl = new StringBuilder();
        ticketRedirectUrl.append(threadRedirectUrl).append(postThreadRequestBodyDTO.getReportId());

        String reportAndPosterTemplate = prepareReportAndPosterTemplate(emailTemplate, report, posterUser, ticketRedirectUrl, timestamp, postThreadRequestBodyDTO);

        String mediaHtml = prepareMediaHtml(mediaDTOS);

        for (String userId : postThreadRequestBodyDTO.getMentions()) {
            userService.findUserById(userId).ifPresent(currentUser -> {
                if (!Objects.equals(currentUser.getId(), posterUser.getId())) {
                    sendUserMentionNotification(currentUser, posterUser, reportAndPosterTemplate, mediaHtml);
                }
            });
        }
    }

    private String prepareReportAndPosterTemplate(String template, Report report, User posterUser, StringBuilder ticketRedirectUrl, String timestamp, PostThreadRequestBodyDTO postThreadRequestBodyDTO) {
        return template.replace("${reportId}", report.getId())
                .replace("${reportTitle}", report.getTitle())
                .replace("${posterName}", posterUser.getFullName())
                .replace("${postedAt}", Objects.requireNonNull(convertToIST(timestamp)))
                .replace("${message}", trimMessage(postThreadRequestBodyDTO.getMessages()))
                .replace("${link}", ticketRedirectUrl.toString());
    }

    private String prepareMediaHtml(ArrayList<ThreadUploadsMediaDTO> mediaDTOS) {
        StringBuilder mediaUrlsBuilder = new StringBuilder();
        for (ThreadUploadsMediaDTO mediaDTO : mediaDTOS) {
            mediaUrlsBuilder.append(generateMediaHtml(mediaDTO)).append("<br>");
        }
        return mediaUrlsBuilder.toString();
    }

    private String prepareFinalHtml(String template, User currentUser, String mediaHtml) {
        return template.replace("${media}", mediaHtml)
                .replace("${emailAddress}", currentUser.getWorkEmail());
    }

    private void sendUserMentionNotification(User currentUser, User posterUser, String template, String mediaHtml) {
        try {
            String finalPreparedHtml = prepareFinalHtml(template, currentUser, mediaHtml);
            String subject = posterUser.getFullName() + " mentioned you in a comment on Quash";
            sendMail(currentUser, subject, "Quash", finalPreparedHtml);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send mention notification", e);
        }
    }


    public static String convertToIST(String timestamp) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestamp);
        ZonedDateTime istTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        return istTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    private String generateMediaHtml(ThreadUploadsMediaDTO mediaDTO) {
        String mediaUrl = mediaDTO.getUrl();
        return switch (mediaDTO.getMediaType()) {
            case "IMAGE" -> "<img src=\"" + mediaUrl + "\" style=\"max-width: 100%; height: auto;\" />";
            case "VIDEO" -> "<a href=\"" + mediaUrl + "\" target=\"_blank\">View Video</a>";
            case "AUDIO" -> "<a href=\"" + mediaUrl + "\" target=\"_blank\">Listen to Audio</a>";
            case "PDF" -> "<a href=\"" + mediaUrl + "\" target=\"_blank\">View PDF</a>";
            case "CRASH" -> "<a href=\"" + mediaUrl + "\" target=\"_blank\">View Crash Report</a>";
            default -> "<a href=\"" + mediaUrl + "\" target=\"_blank\">View Media</a>";
        };
    }

    private String trimMessage(String message) {
        return message.replaceAll("@\\[(.*?)\\]\\([^)]+\\)", "@$1");
    }
}