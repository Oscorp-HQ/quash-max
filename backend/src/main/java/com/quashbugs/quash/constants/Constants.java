package com.quashbugs.quash.constants;

import java.util.Set;

public class Constants {
    public static final int MAX_RETRIES = 3;

    public static final Set<String> ALLOWED_IMAGE_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/*"
    );
    public static final Set<String> ALLOWED_GIF_MIME_TYPES = Set.of(
            "image/gif"
    );
    public static final Set<String> ALLOWED_PDF_MIME_TYPES = Set.of(
            "application/pdf"
    );

    public static final Set<String> ALLOWED_VIDEO_MIME_TYPES = Set.of(
            "video/mp4", "video/avi"
    );

    public static final Set<String> ALLOWED_AUDIO_MIME_TYPES = Set.of(
            "audio/mpeg",   // for MP3
            "audio/wav",    // for WAV
            "audio/aac",    // for AAC
            "audio/ogg",    // for OGG
            "audio/webm"    // for WebM
    );

    public static final String QUASH = "QUASH";

    public static final String INVITE = "INVITE";

    public static final String ALLOWED_TXT_MIME_TYPE = "text/plain";

    public static final String ADMIN = "ADMIN";

    public static final String MEMBER = "MEMBER";

    public static final String VERIFIED = "VERIFIED";

    public static final String JIRA = "JIRA";

    public static final String grant_type = "grant_type";

    public static final String authorization_code = "authorization_code";

    public static final String refresh_token = "refresh_token";

    public static final String projectId = "projectId";

    public static final String issueTypes = "issueTypes";

    public static final String projects = "projects";

    public static final String projectKey = "projectKey";

    public static final String issueTypeKey = "issueTypeKey";

    public static final String paragraph = "paragraph";

    public static final String content = "content";

    public static final String summary = "summary";

    public static final String doc = "doc";

    public static final String version = "version";

    public static final String description = "description";

    public static final String labels = "labels";

    public static final String reporter = "reporter";

    public static final String fields = "fields";

    public static final String issueUpdates = "issueUpdates";

    public static final String issues = "issues";

    public static final String errors = "errors";

    public static final String failedElementNumber = "failedElementNumber";

    public static final String reportId = "reportId";

    public static final String jiraIssueId= "jiraIssueId";

    public static final String SLACK = "SLACK";

    public static final String GITHUB = "GITHUB";

    public static final String GITHUB_API_BASE_URL = "https://api.github.com";

    public static final String LINEAR = "LINEAR";

    public static final String GOOGLE = "GOOGLE";

    public static final String encryptedAccessToken = "encryptedAccessToken";

    public static final String access_token = "access_token";

    public static final String slackUserId = "slackUserId";

    public static final String userId = "userId";

    public static final String teamId = "teamId";

    public static final String team = "team";

    public static final String channels = "channels";

    public static final String channelId = "channelId";

    public static final String client_id = "client_id";

    public static final String client_secret = "client_secret";

    public static final String code = "code";

    public static final String redirect_uri = "redirect_uri";

    public static final String member_joined_channel = "member_joined_channel";

    public static final String channel_left = "channel_left";

    public static final String group_left = "group_left";

    public static final String authed_user = "authed_user";

    public static final String bot_user_id = "bot_user_id";

    public static final String type = "type";

    public static final String text = "text";

    public static final String section = "section";

    public static final String mrkdwn = "mrkdwn";

    public static final String divider = "divider";

    public static final String context = "context";

    public static final String elements = "elements";

    public static final String permalink = "permalink";

    public static final String SECRET_KEY = "YOUR_GENERATED_SECRET";

    public static final long FIFTEEN_MB = 15 * 1024 * 1024; // 15 MB in bytes
}