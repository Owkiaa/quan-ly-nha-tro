package com.nhatro.service;

import com.nhatro.entity.HopDong;
import com.nhatro.repository.HopDongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Tác vụ nền định kỳ kiểm tra và tự động chuyển trạng thái hợp đồng thuê phòng đã quá hạn.
 * Tách riêng khỏi HopDongService để đảm bảo cuộc gọi đi qua Spring AOP Proxy, kích hoạt @Transactional chuẩn xác.
 * 
 * Phụ trách bởi: Bùi Trần Tấn Tài
 */
@Component
public class HopDongScheduler {

    private static final Logger log = LoggerFactory.getLogger(HopDongScheduler.class);

    @Autowired
    private HopDongRepository hopDongRepository;

    @Autowired
    private HopDongService hopDongService;

    /**
     * Tự động quét hợp đồng quá hạn lúc 00:05 hằng ngày.
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void autoMarkExpiredContracts() {
        String threadName = Thread.currentThread().getName();
        LocalDate today = LocalDate.now();
        List<HopDong> expiredContracts = hopDongRepository.findExpiredContracts(today);

        if (!expiredContracts.isEmpty()) {
            log.info("[{}] [HopDongScheduler] Phát hiện {} hợp đồng đã quá ngày kết thúc, tiến hành cập nhật trạng thái HET_HAN...",
                    threadName, expiredContracts.size());

            for (HopDong hd : expiredContracts) {
                try {
                    hopDongService.markContractExpired(hd.getId());
                } catch (Exception e) {
                    log.error("[{}] [HopDongScheduler] Lỗi khi cập nhật hợp đồng hết hạn ID {}: {}",
                            threadName, hd.getId(), e.getMessage(), e);
                }
            }
        }
    }
}
