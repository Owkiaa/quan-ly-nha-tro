package com.nhatro.util;

import com.nhatro.entity.PhongTro;
import com.nhatro.repository.PhongTroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyBookingTest {

    @Autowired
    private PhongTroRepository phongTroRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void testDatPhongDongThoi() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // Dùng startLatch để giữ tất cả các luồng lại, chờ lệnh "bóp cò" đồng loạt
        CountDownLatch startLatch = new CountDownLatch(1);
        // Dùng endLatch để đợi tất cả các luồng chạy xong
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    // Chờ hiệu lệnh xuất phát đồng loạt
                    startLatch.await();

                    Boolean result = transactionTemplate.execute(status -> {
                        PhongTro phongTam = phongTroRepository.findByMaPhong("P201").orElse(null);
                        if (phongTam == null) return false;

                        // Khóa bi quan (Pessimistic Lock)
                        PhongTro phong = phongTroRepository.findByIdForUpdate(phongTam.getId()).orElse(null);

                        if (phong != null && "TRONG".equals(phong.getTrangThai())) {
                            // Giả lập độ trễ cực nhỏ để ép các luồng khác phải chờ khóa
                            try { Thread.sleep(50); } catch (InterruptedException e) {}

                            phong.setTrangThai("DA_DAT");
                            phongTroRepository.save(phong);
                            return true;
                        }
                        return false;
                    });

                    if (Boolean.TRUE.equals(result)) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Bóp cò cho 10 luồng lao vào cùng một thời điểm!
        startLatch.countDown();

        // Chờ tất cả hoàn tất
        endLatch.await();
        executorService.shutdown();

        System.out.println("Số giao dịch đặt phòng thành công: " + successCount.get());
        System.out.println("Số giao dịch bị từ chối do trùng lặp: " + failCount.get());

        assertEquals(1, successCount.get(), "Chỉ 1 người được đặt phòng thành công");
        assertEquals(9, failCount.get(), "9 người còn lại phải nhận thông báo thất bại");
    }
}