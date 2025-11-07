package com.gravifox.domain.issue.controller;

import com.gravifox.domain.issue.domain.SupportIssue;
import com.gravifox.domain.issue.repository.SupportIssueRepository;
import com.gravifox.security.jwt.principal.UserPrincipal;
import com.gravifox.domain.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/issue")
@RequiredArgsConstructor
public class GithubIssueController {

    private final SupportIssueRepository supportIssueRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public ResponseEntity<List<Map<String, Object>>> getIssues() {
        List<SupportIssue> all = supportIssueRepository.findAll();
        // newest first
        all.sort((a, b) -> {
            LocalDateTime la = a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt();
            LocalDateTime lb = b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt();
            if (la == null && lb == null) return 0;
            if (la == null) return 1;
            if (lb == null) return -1;
            return lb.compareTo(la);
        });

        List<Map<String, Object>> payload = all.stream().map(this::toInboxShape).toList();
        return ResponseEntity.ok(payload);
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createIssue(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name = "title", required = false, defaultValue = "") String title,
            @RequestParam(name = "body", required = false) String body,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "category", required = false, defaultValue = "uncategorized") String category,
            @RequestParam(name = "attachment", required = false) MultipartFile attachment
    ) {
        Long userNo = principal != null ? Long.parseLong(principal.getName()) : null;

        SupportIssue issue = new SupportIssue();
        issue.setUserNo(userNo);
        if (userNo != null) {
            userRepository.findById(userNo).ifPresent(u -> issue.setEmail(u.getUserId()));
        }
        issue.setCategory(category);
        issue.setTitle(title);
        String content = body != null ? body : (description != null ? description : "");
        issue.setBody(content);
        issue.setState("open");

        SupportIssue saved = supportIssueRepository.save(issue);
        Map<String, Object> resp = toInboxShape(saved);
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toInboxShape(SupportIssue e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("number", e.getId());
        m.put("title", e.getTitle());
        m.put("body", e.getBody());
        m.put("category", e.getCategory());
        m.put("state", e.getState() == null ? "open" : e.getState());
        m.put("createdAt", e.getCreatedAt());
        m.put("updatedAt", e.getUpdatedAt());
        m.put("reporterEmail", e.getEmail());
        m.put("reporterUserNo", e.getUserNo());
        return m;
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<?> updateIssueState(@PathVariable("id") Long id,
                                              @RequestParam("state") String state) {
        Optional<SupportIssue> opt = supportIssueRepository.findById(id);
        if (opt.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "해당 이슈를 찾을 수 없습니다.");
            return ResponseEntity.status(404).body(err);
        }
        String normalized = state == null ? "" : state.trim().toLowerCase();
        if (!normalized.equals("open") && !normalized.equals("closed")) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "state 값은 open 또는 closed 여야 합니다.");
            return ResponseEntity.badRequest().body(err);
        }
        SupportIssue issue = opt.get();
        issue.setState(normalized);
        SupportIssue saved = supportIssueRepository.save(issue);
        return ResponseEntity.ok(toInboxShape(saved));
    }
}
