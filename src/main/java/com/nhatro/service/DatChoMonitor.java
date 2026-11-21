package com.nhatro.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Monitor đồng bộ tài nguyên dùng chung theo chuẩn Chương 4 (Synchronized Monitor Lock).
 * Liên kết trạng thái hoạt động giữa các luồng:
 * - Luồng Web MVC (http-nio-8080-exec-...)
 * - Luồng Socket Server (client-1, client-2, client-3...)
 * - Luồng Tác vụ nền @Scheduled (scheduling-1)
 */
@Component
public class DatChoMonitor {

    private static final Logger log = LoggerFactory.getLogger(DatChoMonitor.class);
    private static final int MAX_LOG_LINES = 15;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private int soClientDangKetNoi = 0;
    private int soLuotDatThanhCong = 0;
    private int soLuotBiTuChoi = 0;
    private int soPhongGiaiPhongQuaHan = 0;
    private final LinkedList<String> nhatKyDaLuong = new LinkedList<>();

    public synchronized void ghiNhanClientKetNoi(String diaChi) {
        soClientDangKetNoi++;
        themNhatKy("Socket Client kết nối từ " + diaChi + " (Đang online: " + soClientDangKetNoi + ")");
    }

    public synchronized void ghiNhanClientNgatKetNoi(String diaChi) {
        if (soClientDangKetNoi > 0) {
            soClientDangKetNoi--;
        }
        themNhatKy("Socket Client ngắt kết nối " + diaChi + " (Còn online: " + soClientDangKetNoi + ")");
    }

    public synchronized void ghiNhanDatPhongThanhCong(String maPhong, String hoTen, String cccd, String nguon) {
        soLuotDatThanhCong++;
        String moTaNguon = (nguon != null && !nguon.trim().isEmpty()) ? nguon.trim() : "Web";
        themNhatKy("Đặt phòng " + maPhong + " THÀNH CÔNG cho " + hoTen + " (CCCD: " + cccd + ") [" + moTaNguon + "]");
    }

    public synchronized void ghiNhanDatPhongBiTuChoi(String maPhong, String lyDo, String nguon) {
        soLuotBiTuChoi++;
        String moTaNguon = (nguon != null && !nguon.trim().isEmpty()) ? nguon.trim() : "Web";
        themNhatKy("Chặn đặt trùng phòng " + maPhong + " (" + lyDo + ") [" + moTaNguon + "]");
    }

    public synchronized void ghiNhanGiaiPhongQuaHan(String maPhong, Integer datChoId) {
        soPhongGiaiPhongQuaHan++;
        themNhatKy("Tác vụ @Scheduled giải phóng phòng " + maPhong + " (Đặt chỗ #" + datChoId + ") về TRONG");
    }

    public synchronized void themNhatKy(String noiDung) {
        String thoiGian = LocalTime.now().format(TIME_FORMATTER);
        String tenLuong = Thread.currentThread().getName();
        String dongLog = thoiGian + " [" + tenLuong + "] " + noiDung;
        nhatKyDaLuong.addFirst(dongLog);
        while (nhatKyDaLuong.size() > MAX_LOG_LINES) {
            nhatKyDaLuong.removeLast();
        }
    }

    public synchronized int getSoClientDangKetNoi() {
        return soClientDangKetNoi;
    }

    public synchronized int getSoLuotDatThanhCong() {
        return soLuotDatThanhCong;
    }

    public synchronized int getSoLuotBiTuChoi() {
        return soLuotBiTuChoi;
    }

    public synchronized int getSoPhongGiaiPhongQuaHan() {
        return soPhongGiaiPhongQuaHan;
    }

    public synchronized List<String> getNhatKyDaLuong() {
        return new ArrayList<>(nhatKyDaLuong);
    }

    /**
     * Đối tượng mô phỏng tài nguyên dùng chung (phòng trọ) minh họa Chương 4:
     * So sánh phương thức không đồng bộ (Race Condition) vs phương thức có synchronized Monitor Lock.
     */
    public static class PhongTroMoPhong {
        private String trangThai = "TRONG";

        public boolean datPhongKhongDongBo() {
            if ("TRONG".equals(trangThai)) {
                try {
                    Thread.sleep(10); // Mô phỏng độ trễ kiểm tra / ghi dữ liệu giữa các luồng
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                trangThai = "DA_DAT";
                return true;
            }
            return false;
        }

        public synchronized boolean datPhongAnToan() {
            if ("TRONG".equals(trangThai)) {
                try {
                    Thread.sleep(10); // Giữ Monitor Lock nên các luồng khác phải chờ ở trạng thái Seeking Lock
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                trangThai = "DA_DAT";
                return true;
            }
            return false;
        }

        public String getTrangThai() {
            return trangThai;
        }
    }

    /**
     * Chạy trực tiếp 2 kịch bản đa luồng bằng Thread + Runnable + start() + join():
     * 1) Không đồng bộ -> Xảy ra Race Condition (nhiều luồng cùng đặt trùng 1 phòng).
     * 2) Có từ khóa synchronized -> Chỉ 1 luồng đặt thành công, các luồng còn lại bị từ chối.
     */
    public String chayMoPhongDongBoChuong4(int soLuongKhach) {
        int count = Math.max(1, soLuongKhach);
        themNhatKy("=== Bắt đầu mô phỏng Đồng bộ Chương 4 với " + count + " luồng Thread ===");

        // Phần 1: Không đồng bộ (Race Condition)
        PhongTroMoPhong phongKhongDongBo = new PhongTroMoPhong();
        final int[] thanhCongKhongDongBo = {0};
        Thread[] threadsKhongDongBo = new Thread[count];

        for (int i = 0; i < count; i++) {
            final int idx = i + 1;
            threadsKhongDongBo[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean ok = phongKhongDongBo.datPhongKhongDongBo();
                    if (ok) {
                        synchronized (thanhCongKhongDongBo) {
                            thanhCongKhongDongBo[0]++;
                        }
                    }
                    String msg = "Không đồng bộ -> " + (ok ? "THÀNH CÔNG (Bị trùng do Race Condition!)" : "Thất bại");
                    log.info("[{}] {}", Thread.currentThread().getName(), msg);
                    themNhatKy(msg);
                }
            }, "Khach-NoSync-" + idx);
        }

        for (Thread t : threadsKhongDongBo) {
            t.start();
        }
        for (Thread t : threadsKhongDongBo) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Phần 2: Có đồng bộ bằng Monitor Lock (synchronized)
        PhongTroMoPhong phongAnToan = new PhongTroMoPhong();
        final int[] thanhCongCoDongBo = {0};
        final int[] tuChoiCoDongBo = {0};
        Thread[] threadsCoDongBo = new Thread[count];

        for (int i = 0; i < count; i++) {
            final int idx = i + 1;
            threadsCoDongBo[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean ok = phongAnToan.datPhongAnToan();
                    synchronized (thanhCongCoDongBo) {
                        if (ok) {
                            thanhCongCoDongBo[0]++;
                        } else {
                            tuChoiCoDongBo[0]++;
                        }
                    }
                    String msg = "Có synchronized -> " + (ok ? "THÀNH CÔNG (Giữ Monitor Lock)" : "BỊ TỪ CHỐI (Phòng đã DA_DAT)");
                    log.info("[{}] {}", Thread.currentThread().getName(), msg);
                    themNhatKy(msg);
                }
            }, "Khach-Sync-" + idx);
        }

        for (Thread t : threadsCoDongBo) {
            t.start();
        }
        for (Thread t : threadsCoDongBo) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        String ketQua = "Không đồng bộ (Race Condition): " + thanhCongKhongDongBo[0] + "/" + count
                + " luồng cùng đặt trùng phòng! | Có đồng bộ (synchronized Chương 4): Đúng "
                + thanhCongCoDongBo[0] + "/" + count + " luồng đặt thành công, "
                + tuChoiCoDongBo[0] + "/" + count + " luồng bị từ chối!";
        themNhatKy("Kết quả mô phỏng: " + ketQua);
        return ketQua;
    }
}
