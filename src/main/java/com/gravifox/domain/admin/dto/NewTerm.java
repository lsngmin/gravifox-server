package com.gravifox.domain.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NewTerm {
    private String termKey;
    private String termTitle;
    private String termContent;
    private Boolean isRequired;
    private String termVersion;
    private LocalDateTime effectiveAt;
}
