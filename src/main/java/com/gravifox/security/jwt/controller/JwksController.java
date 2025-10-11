package com.gravifox.security.jwt.controller;

import com.gravifox.domain.file.token.UploadTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/.well-known")
public class JwksController {

    private final UploadTokenService uploadTokenService;

    @GetMapping("/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok(Map.of("keys", uploadTokenService.jwks()));
    }
}
