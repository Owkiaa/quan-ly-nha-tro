package com.nhatro.util;

import com.nhatro.entity.PhongTro;
import com.nhatro.repository.PhongTroRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyBookingTest {

    private static final String SOCKET_HOST = "localhost";
    private static final int SOCKET_PORT = 9999;
    private static final String TEST_ROOM_CODE = "P201";

    @Autowired
    private PhongTroRepository phongTroRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * Chương trình kiểm thử đồng thời độc lập chạy hàm main() qua Socket (Port 9999),
     * sử dụng 10 luồng với 10 số CCCD khác nhau đồng loạt gửi lệnh BOOK P201.
     */
    public static void main(String[] args) throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger okCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        System.out.println("=== BẮT ĐẦU KIỂM THỬ ĐỒNG THỜI QUA SOCKET (10 LUỒNG - 10 CCCD) ===");

        for (int i = 0; i < numberOfThreads; i++) {
            final int clientIdx = i + 1;
            final String hoTen = "Khach Kiem Thu " + clientIdx;
            final String cccd = String.format("0792040000%02d", clientIdx);
            final String sdt = String.format("09012345%02d", clientIdx);

            pool.execute(() -> {
                String threadName = Thread.currentThread().getName();
                try (Socket socket = new Socket(SOCKET_HOST, SOCKET_PORT);
                     PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                    // Đọc dòng chào trước khi sẵn sàng xuất phát
                    in.readLine();

                    readyLatch.countDown();
                    startLatch.await();

                    String command = "BOOK " + TEST_ROOM_CODE + "#" + hoTen + "#" + cccd + "#" + sdt;
                    out.println(command);
                    String response = in.readLine();
                    System.out.println("[" + threadName + "] Client #" + clientIdx + " (CCCD: " + cccd + ") -> " + response);

                    if (response != null && response.startsWith("OK#")) {
                        okCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }

                    out.println("EXIT");
                    in.readLine();
                } catch (Exception e) {
                    System.err.println("[" + threadName + "] Client #" + clientIdx + " lỗi: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    if (readyLatch.getCount() > 0 && startLatch.getCount() > 0) {
                        readyLatch.countDown();
                    }
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        System.out.println("Tất cả 10 luồng đã kết nối Socket, đồng loạt gửi lệnh BOOK " + TEST_ROOM_CODE + "...");
        startLatch.countDown();

        doneLatch.await();
        pool.shutdown();

        System.out.println("====================================================");
        System.out.println("Kết quả đặt thành công (OK): " + okCount.get() + " (Kỳ vọng: 1)");
        System.out.println("Kết quả bị từ chối (FAIL):   " + failCount.get() + " (Kỳ vọng: 9)");
    }

    @Test
    public void testDatPhongDongThoi() throws InterruptedException {
        Boolean roomReady = transactionTemplate.execute(status -> {
            PhongTro p = phongTroRepository.findByMaPhong(TEST_ROOM_CODE).orElse(null);
            if (p == null) {
                return false;
            }
            p.setTrangThai("TRONG");
            phongTroRepository.save(p);
            return true;
        });
        Assumptions.assumeTrue(Boolean.TRUE.equals(roomReady),
                "Bỏ qua test vì chưa có phòng " + TEST_ROOM_CODE + " trong CSDL");

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    startLatch.await();

                    Boolean result = transactionTemplate.execute(status -> {
                        PhongTro phongTam = phongTroRepository.findByMaPhong(TEST_ROOM_CODE).orElse(null);
                        if (phongTam == null) {
                            return false;
                        }

                        // Khóa bi quan (Pessimistic Lock)
                        PhongTro phong = phongTroRepository.findByIdForUpdate(phongTam.getId()).orElse(null);

                        if (phong != null && "TRONG".equals(phong.getTrangThai())) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException ignored) {
                            }

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

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        System.out.println("Số giao dịch đặt phòng thành công: " + successCount.get());
        System.out.println("Số giao dịch bị từ chối do trùng lặp: " + failCount.get());

        assertEquals(1, successCount.get(), "Chỉ 1 người được đặt phòng thành công");
        assertEquals(9, failCount.get(), "9 người còn lại phải nhận thông báo thất bại");
    }
}
