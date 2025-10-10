package com.gravifox.logging.util;

import lombok.Builder;

@Builder
public class LogMessage {
    private LogUtil logUtil;

    private String className;
    private String action;
    private String status;
    private String detail;

    private String method;
    private String uri;
    private String code;
    private String value;

    public String formatMessage() {
        return
                String.format("[%s] ", className) +
                String.format("%s ", action) +
                String.format("%s ", status) +
                String.format("%s", detail) +
                String.format("(%s)", value) +
                " - \"" +
                String.format("%-4s ", method) +
                String.format("%s ", uri) +
                String.format("%s", code) +
                "\"";
    }
}
