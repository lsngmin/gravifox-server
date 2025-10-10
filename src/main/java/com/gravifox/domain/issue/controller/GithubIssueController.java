package com.gravifox.domain.issue.controller;

import com.gravifox.domain.issue.service.GitHubDataService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/issue")
@RequiredArgsConstructor
@AllArgsConstructor
public class GithubIssueController {
    @Autowired
    private GitHubDataService gitHubDataService;
    @GetMapping("/")
    public ResponseEntity<?> getIssues()  {
        String issues = gitHubDataService.fetchIssueData();
        return ResponseEntity.ok(issues);
    }
}
