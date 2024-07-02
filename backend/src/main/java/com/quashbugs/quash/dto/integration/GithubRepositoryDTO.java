package com.quashbugs.quash.dto.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepositoryDTO {
    private Long id;

    @JsonProperty("node_id")
    private String nodeId;

    private String name;

    @JsonProperty("full_name")
    private String fullName;

    private GitHubUser owner;

    private Boolean privateRepo;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String description;

    private Boolean fork;

    private String url;

    @JsonProperty("archive_url")
    private String archiveUrl;

    public static class GitHubUser {
        private String login;
        private Long id;
        @JsonProperty("avatar_url")
        private String avatarUrl;
    }
}
