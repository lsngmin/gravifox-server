package com.gravifox.logging.util;

public enum LogStatus {
    OK("SUCCESS"),
    FAILURE("FAILURE"),
    ERROR("ERROR"),
    PENDING("PENDING"),
    IN_PROGRESS("IN_PROGRESS");


    private final String status;
    public String getStatus() {
        return this.status;
    }
    LogStatus(String status) {
        this.status = status;
    }
}
