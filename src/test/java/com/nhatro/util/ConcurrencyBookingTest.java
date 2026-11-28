package com.nhatro.util;

import com.nhatro.entity.PhongTro;
import com.nhatro.repository.PhongTroRepository;
import com.nhatro.service.DatChoMonitor;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ConcurrencyBookingTest {

    private static final String SOCKET_HOST = "localhost";
    private static final int SOCKET_PORT = 9999;
    private static final String TEST_ROOM_CODE = "P201";

    @Autowired
    private PhongTroRepository phongTroRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DatChoMonitor datChoMonitor;

    /**
     * Chương trình kiểm thử độc lập chạy bằng hàm main():
     * - Phần 1: Minh họa Đồng bộ Chương 4 (Thread, Runnable, start(), join(), synchronized Monitor Lock).
     * - Phần 2: Kiểm thử Đa luồng qua Socket Server Port 9999 với 10 luồng Thread và 10 số CCCD khác nhau.
     */
    public static void main(String[] args) throws InterruptedException {
        chayPhan1MinhHoaDongBoChuong4();
        chayPhan2KiemThuSocket10Luong();
    }

    /**
     * Phần 1: Minh họa Race Condition vs synchronized theo đúng mẫu Chương 4 (Cau4.java).
     */
    private static void chayPhan1MinhHoaDongBoChuong4() throws InterruptedException {
        System.out.println("=== PHẦN 1: MINH HỌA ĐỒNG BỘ CHƯƠNG 4 (THREAD + RUNNABLE + SYNCHRONIZED) ===");
        int soLuongKhach = 5;

        // 1a. Không đồng bộ -> Xảy ra Race Condition
        DatChoMonitor.PhongTroMoPhong phongKhongDongBo = new DatChoMonitor.PhongTroMoPhong();
        AtomicInteger okKhongDongBo = new AtomicInteger(0);
        Thread[] threadsKhongDongBo = new Thread[soLuongKhach];

        for (int i = 0; i < soLuongKhach; i++) {
            final int khachIdx = i + 1;
            threadsKhongDongBo[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean ok = phongKhongDongBo.datPhongKhongDongBo();
                    if (ok) {
                        okKhongDongBo.incrementAndGet();
                    }
                    System.out.println("[" + Thread.currentThread().getName() + "] Không đồng bộ -> "
                            + (ok ? "THÀNH CÔNG (Bị đặt trùng!)" : "Thất bại"));
                }
            }, "Khach-NoSync-" + khachIdx);
        }
        for (Thread t : threadsKhongDongBo) {
            t.start();
        }
        for (Thread t : threadsKhongDongBo) {
            t.join();
        }
        System.out.println("-> Kết quả Không đồng bộ: " + okKhongDongBo.get() + "/" + soLuongKhach + " luồng cùng đặt thành công (LỖI Race Condition)");

        // 1b. Có từ khóa synchronized -> Chỉ 1 luồng giữ Monitor Lock và đặt thành công
        DatChoMonitor.PhongTroMoPhong phongAnToan = new DatChoMonitor.PhongTroMoPhong();
        AtomicInteger okCoDongBo = new AtomicInteger(0);
        AtomicInteger failCoDongBo = new AtomicInteger(0);
        Thread[] threadsCoDongBo = new Thread[soLuongKhach];

        for (int i = 0; i < soLuongKhach; i++) {
            final int khachIdx = i + 1;
            threadsCoDongBo[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean ok = phongAnToan.datPhongAnToan();
                    if (ok) {
                        okCoDongBo.incrementAndGet();
                    } else {
                        failCoDongBo.incrementAndGet();
                    }
                    System.out.println("[" + Thread.currentThread().getName() + "] Có synchronized -> "
                            + (ok ? "THÀNH CÔNG (Giữ Monitor Lock)" : "BỊ TỪ CHỐI (Phòng đã DA_DAT)"));
                }
            }, "Khach-Sync-" + khachIdx);
        }
        for (Thread t : threadsCoDongBo) {
            t.start();
        }
        for (Thread t : threadsCoDongBo) {
            t.join();
        }
        System.out.println("-> Kết quả Có synchronized: Thành công = " + okCoDongBo.get()
                + ", Bị từ chối = " + failCoDongBo.get() + " (ĐÚNG)");
        System.out.println();
    }

    /**
     * Phần 2: Khởi tạo 10 luồng Thread[] đồng loạt gửi lệnh BOOK P201 qua Socket Server cổng 9999.
     */
    private static void chayPhan2KiemThuSocket10Luong() throws InterruptedException {
        int numberOfThreads = 10;
        Thread[] clientThreads = new Thread[numberOfThreads];
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger okCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        System.out.println("=== PHẦN 2: KIỂM THỬ ĐỒNG THỜI QUA SOCKET SERVER (10 LUỒNG - 10 CCCD) ===");

        for (int i = 0; i < numberOfThreads; i++) {
            final int clientIdx = i + 1;
            final String hoTen = "Khach Kiem Thu " + clientIdx;
            final String cccd = String.format("0792040000%02d", clientIdx);
            final String sdt = String.format("09012345%02d", clientIdx);

            clientThreads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
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
                    }
                }
            }, "client-test-" + clientIdx);

            clientThreads[i].start();
        }

        readyLatch.await();
        System.out.println("Tất cả 10 luồng đã kết nối Socket, đồng loạt gửi lệnh BOOK " + TEST_ROOM_CODE + "...");
        startLatch.countDown();

        for (Thread t : clientThreads) {
            t.join();
        }

        System.out.println("====================================================");
        System.out.println("Kết quả đặt thành công (OK): " + okCount.get() + " (Kỳ vọng: 1)");
        System.out.println("Kết quả bị từ chối (FAIL):   " + failCount.get() + " (Kỳ vọng: 9)");
    }

    @Test
    public void testMoPhongDongBoChuong4() {
        String ketQua = datChoMonitor.chayMoPhongDongBoChuong4(5);
        System.out.println(ketQua);
        assertTrue(ketQua.contains("Đúng 1/5 luồng đặt thành công, 4/5 luồng bị từ chối"),
                "Cơ chế synchronized Chương 4 phải đảm bảo đúng 1/5 luồng thành công");
    }

    @Test
    public void testDatPhongDongThoi() throws InterruptedException {
        Integer roomId = transactionTemplate.execute(status -> {
            PhongTro p = phongTroRepository.findByMaPhong(TEST_ROOM_CODE).orElse(null);
            if (p == null) {
                return null;
            }
            p.setTrangThai("TRONG");
            phongTroRepository.save(p);
            return p.getId();
        });
        Assumptions.assumeTrue(roomId != null,
                "Bỏ qua test vì chưa có phòng " + TEST_ROOM_CODE + " trong CSDL");

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        try {
            for (int i = 0; i < numberOfThreads; i++) {
                executorService.execute(() -> {
                    try {
                        startLatch.await();

                        Boolean result = transactionTemplate.execute(status -> {
                            // Khóa bi quan (Pessimistic Lock) là truy vấn đầu tiên trong Transaction để tránh Hibernate L1 Cache cũ
                            PhongTro phong = phongTroRepository.findByIdForUpdate(roomId).orElse(null);

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
        } finally {
            transactionTemplate.execute(status -> {
                phongTroRepository.findById(roomId).ifPresent(p -> {
                    p.setTrangThai("TRONG");
                    phongTroRepository.save(p);
                });
                return null;
            });
        }
    }
}
