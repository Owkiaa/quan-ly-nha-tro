package com.nhatro.service;

import com.nhatro.entity.DatCho;
import com.nhatro.repository.DatChoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp lập lịch định kỳ quét và giải phóng đặt giữ chỗ phòng quá hạn.
 * 
 * Thiết kế khắc phục lỗi Spring AOP Proxy (Self-Invocation):
 * Tách riêng DatChoScheduler tiêm phụ thuộc DatChoService Bean. Khi gọi
 * datChoService.releaseSingleBooking(...), cuộc gọi đi qua Spring Dynamic Proxy
 * bao bọc DatChoService, đảm bảo @Transactional và Pessimistic Write Lock
 * (findByIdForUpdate) được kích hoạt chuẩn xác trong Transaction riêng biệt cho từng bản ghi.
 * 
 * Phụ trách chính: Bùi Trần Tấn Tài
 */
@Component
public class DatChoScheduler {

    private static final Logger log = LoggerFactory.getLogger(DatChoScheduler.class);

    @Autowired
    private DatChoRepository datChoRepository;

    @Autowired
    private DatChoService datChoService; // Inject Service để đảm bảo đi qua Spring Proxy

    /**
     * Quét định kỳ mỗi 60 giây (fixedDelay = 60000ms), bắt đầu sau khi ứng dụng chạy 10 giây (initialDelay = 10000ms).
     * Ghi rõ Thread.currentThread().getName() (ví dụ: [scheduling-1]) để phục vụ chụp hình làm minh chứng báo cáo.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void autoReleaseExpiredBookings() {
        runScanAndRelease(false);
    }

    /**
     * Quét và giải phóng thủ công toàn bộ đặt chỗ quá hạn thông qua DatChoService Bean
     * để đảm bảo Spring AOP Proxy kích hoạt @Transactional và Pessimistic Lock (findByIdForUpdate).
     */
    public int quetVaGiaiPhongDatChoQuaHan() {
        return runScanAndRelease(true);
    }

    private int runScanAndRelease(boolean logWhenEmpty) {
        String threadName = Thread.currentThread().getName();
        List<DatCho> expiredList = datChoRepository.findExpiredBookings(LocalDateTime.now());
        if (expiredList.isEmpty()) {
            if (logWhenEmpty) {
                log.info("[{}] [DatChoScheduler] Bắt đầu quét đặt chỗ quá hạn: không tìm thấy bản ghi nào", threadName);
            }
            return 0;
        }

        log.info("[{}] [DatChoScheduler] Phát hiện {} bản ghi đặt giữ chỗ quá hạn cần giải phóng",
                threadName, expiredList.size());

        int count = 0;
        for (DatCho dc : expiredList) {
            try {
                // Gọi qua Spring Bean DatChoService để @Transactional và findByIdForUpdate hoạt động
                datChoService.releaseSingleBooking(dc.getId(), dc.getPhongTro().getId());
                count++;
            } catch (Exception e) {
                log.error("[{}] [DatChoScheduler] Lỗi khi giải phóng đặt chỗ ID {}: {}",
                        threadName, dc.getId(), e.getMessage(), e);
            }
        }

        log.info("[{}] [DatChoScheduler] Hoàn tất đợt quét và xử lý {} đặt chỗ quá hạn", threadName, count);
        return count;
    }
}
