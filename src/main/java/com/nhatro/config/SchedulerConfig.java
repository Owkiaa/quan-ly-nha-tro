package com.nhatro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cấu hình kích hoạt tác vụ nền lập lịch (Background Scheduled Tasks) trong Spring Boot.
 * Phục vụ cơ chế tự động quét và giải phóng đặt giữ chỗ phòng quá hạn.
 * 
 * Phụ trách chính: Bùi Trần Tấn Tài
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
