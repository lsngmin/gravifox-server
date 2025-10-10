package com.gravifox.domain.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubIssue {
    private long id; // Primary Issue ID
    @JsonProperty("repository_url")
    private String repositoryUrl; // Repos Name Url
    private int number; //issue ID
    private String title; //issue Name
    private String state;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("closed_at")
    private String closedAt;
    private String body;
}
