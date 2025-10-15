-- Analysis report table DDL (MySQL 8.x)
CREATE TABLE `analysis_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_no` BIGINT NOT NULL,
    `upload_id` VARCHAR(64) NOT NULL,
    `media_type` ENUM('image', 'video') NOT NULL DEFAULT 'image',
    `label` ENUM('ai', 'real', 'unknown') NOT NULL,
    `score` DECIMAL(5,4) NOT NULL,
    `model_version` VARCHAR(32) NOT NULL,
    `heatmap_json` JSON NOT NULL,
    `meta_json` JSON NULL,
    `inference_time_ms` INT NULL,
    `input_resolution` VARCHAR(16) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_analysis_report_user_upload` (`user_no`, `upload_id`),
    KEY `idx_analysis_report_user` (`user_no`),
    KEY `idx_analysis_report_user_created_at_desc` (`user_no`, `created_at` DESC),
    KEY `idx_analysis_report_user_media_type` (`user_no`, `media_type`),
    KEY `idx_analysis_report_model_version` (`model_version`),
    CONSTRAINT `fk_analysis_report_user` FOREIGN KEY (`user_no`) REFERENCES `user` (`user_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

