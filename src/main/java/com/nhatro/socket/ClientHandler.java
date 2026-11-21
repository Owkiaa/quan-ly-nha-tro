package com.nhatro.socket;

import com.nhatro.entity.DatCho;
import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.ResourceNotFoundException;
import com.nhatro.exception.RoomAlreadyBookedException;
import com.nhatro.service.DatChoMonitor;
import com.nhatro.service.DatChoService;
import com.nhatro.service.PhongTroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Bộ xử lý cho từng Client Socket chạy trên một luồng trong ExecutorService Thread Pool.
 * Phụ trách bởi: Đoàn Văn Mẩn
 */
public class ClientHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final DatChoService datChoService;
    private final PhongTroService phongTroService;
    private final DatChoMonitor datChoMonitor;

    public ClientHandler(Socket socket, DatChoService datChoService, PhongTroService phongTroService, DatChoMonitor datChoMonitor) {
        this.socket = socket;
        this.datChoService = datChoService;
        this.phongTroService = phongTroService;
        this.datChoMonitor = datChoMonitor;
    }

    public ClientHandler(Socket socket, DatChoService datChoService, PhongTroService phongTroService) {
        this(socket, datChoService, phongTroService, null);
    }

    public ClientHandler(Socket socket) {
        this(socket, null, null, null);
    }

    private DatChoService getDatChoService() {
        return datChoService != null ? datChoService : ApplicationContextProvider.getBean(DatChoService.class);
    }

    private PhongTroService getPhongTroService() {
        return phongTroService != null ? phongTroService : ApplicationContextProvider.getBean(PhongTroService.class);
    }

    private DatChoMonitor getDatChoMonitor() {
        if (datChoMonitor != null) {
            return datChoMonitor;
        }
        try {
            return ApplicationContextProvider.getBean(DatChoMonitor.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        String clientAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        log.info("[{}] Client mới đã kết nối từ {}", threadName, clientAddress);

        DatChoMonitor monitor = getDatChoMonitor();
        if (monitor != null) {
            monitor.ghiNhanClientKetNoi(clientAddress);
        }

        try (Socket clientSocket = this.socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            out.println("OK#Ket noi thanh cong! Cac lenh ho tro: LIST_ROOM | BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt> | EXIT");

            String rawLine;
            while ((rawLine = in.readLine()) != null) {
                String command = rawLine.trim();
                if (command.isEmpty()) {
                    continue;
                }
                log.info("[{}] Nhận lệnh từ client: {}", threadName, command);

                if ("EXIT".equalsIgnoreCase(command)) {
                    out.println("BYE#Tam biet");
                    log.info("[{}] Client yêu cầu ngắt kết nối.", threadName);
                    break;
                } else if ("LIST_ROOM".equalsIgnoreCase(command)) {
                    handleListRoom(out, threadName);
                } else if ("BOOK".equalsIgnoreCase(command) || command.toUpperCase().startsWith("BOOK ")) {
                    handleBook(command, out, threadName);
                } else {
                    out.println("FAIL#Lenh khong hop le. Ho tro: LIST_ROOM, BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt>, EXIT");
                }
            }
        } catch (IOException e) {
            log.error("[{}] Lỗi kết nối Socket Client: {}", threadName, e.getMessage());
        } finally {
            if (monitor != null) {
                monitor.ghiNhanClientNgatKetNoi(clientAddress);
            }
            log.info("[{}] Đã đóng socket và giải phóng tài nguyên luồng.", threadName);
        }
    }

    private void handleListRoom(PrintWriter out, String threadName) {
        try {
            List<PhongTro> danhSachPhongTrong = getDatChoService().layDanhSachPhongTrong();
            if (danhSachPhongTrong == null || danhSachPhongTrong.isEmpty()) {
                out.println("OK#Hien khong co phong trong nao trong he thong.");
            } else {
                StringBuilder sb = new StringBuilder("=== DANH SÁCH PHÒNG TRỌ TRỐNG ===\n");
                for (PhongTro phong : danhSachPhongTrong) {
                    sb.append("Mã phòng: ").append(phong.getMaPhong())
                            .append(" | Tên: ").append(phong.getTenPhong())
                            .append(" | Giá: ").append(phong.getGiaThue())
                            .append(" | Trạng thái: ").append(phong.getTrangThai())
                            .append("\n");
                }
                out.println(sb.toString().trim());
            }
            log.info("[{}] Đã xử lý xong lệnh LIST_ROOM ({} phòng trống).",
                    threadName, danhSachPhongTrong != null ? danhSachPhongTrong.size() : 0);
        } catch (Exception dbEx) {
            out.println("FAIL#Loi truy xuat CSDL: " + dbEx.getMessage());
            log.error("[{}] Lỗi CSDL khi lấy danh sách phòng trống: {}", threadName, dbEx.getMessage(), dbEx);
        }
    }

    private void handleBook(String command, PrintWriter out, String threadName) {
        if (command.length() <= 5) {
            out.println("FAIL#Thieu ma phong. Cu phap: BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt>");
            return;
        }

        String payload = command.substring(5).trim();
        String maPhong;
        String hoTen;
        String cccd;
        String sdt;

        if (payload.contains("#")) {
            String[] parts = payload.split("#", -1);
            if (parts.length < 4 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()
                    || parts[2].trim().isEmpty() || parts[3].trim().isEmpty()) {
                out.println("FAIL#Thieu thong tin dat phong. Cu phap: BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt>");
                return;
            }
            maPhong = parts[0].trim();
            hoTen = parts[1].trim();
            cccd = parts[2].trim();
            sdt = parts[3].trim();
        } else {
            // Hỗ trợ cú pháp rút gọn BOOK <ma_phong>
            maPhong = payload;
            hoTen = "Khách đặt qua Socket";
            cccd = "079204000000";
            sdt = "0900000000";
        }

        try {
            // Tìm ID phòng trước ở giao dịch đọc ngắn, sau đó gọi datChoPhong để findByIdForUpdate làm truy vấn đầu tiên trong Transaction
            PhongTro phong = getPhongTroService().getByMaPhong(maPhong);
            DatCho datCho = getDatChoService().datChoPhong(
                    phong.getId(), hoTen, cccd, sdt, null, "Đặt qua Socket Client", 24);
            out.println("OK#Dat cho thanh cong cho phong " + phong.getMaPhong() + " (ID dat cho: " + datCho.getId() + ")");
            log.info("[{}] Đặt phòng THÀNH CÔNG cho mã {} (CCCD: {})", threadName, phong.getMaPhong(), cccd);
        } catch (RoomAlreadyBookedException bookedEx) {
            out.println("FAIL#Phong da duoc dat hoac dang thue: " + bookedEx.getMessage());
            log.warn("[{}] Từ chối BOOK phòng {}: {}", threadName, maPhong, bookedEx.getMessage());
        } catch (ResourceNotFoundException | BusinessException bizEx) {
            out.println("FAIL#" + bizEx.getMessage());
            log.warn("[{}] Không thể BOOK phòng {}: {}", threadName, maPhong, bizEx.getMessage());
        } catch (Exception dbEx) {
            out.println("FAIL#Loi khi xu ly dat phong: " + dbEx.getMessage());
            log.error("[{}] Lỗi hệ thống khi BOOK phòng {}: {}", threadName, maPhong, dbEx.getMessage(), dbEx);
        }
    }
}
