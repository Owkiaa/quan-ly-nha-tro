package com.nhatro.socket;

import com.nhatro.entity.PhongTro;
import com.nhatro.service.PhongTroService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("Client mới kết nối. Luồng xử lý: " + threadName);

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String command;
            out.println("Kết nối thành công! Các lệnh hỗ trợ: LIST_ROOM, BOOK <mã_phòng>, EXIT");

            while ((command = in.readLine()) != null) {
                System.out.println("[" + threadName + "] Nhận lệnh: " + command);

                if ("EXIT".equalsIgnoreCase(command)) {
                    out.println("Đóng kết nối. Tạm biệt!");
                    break;
                } else if ("LIST_ROOM".equalsIgnoreCase(command)) {
                    try {
                        PhongTroService phongTroService = ApplicationContextProvider.getBean(PhongTroService.class);
                        List<PhongTro> danhSachPhong = phongTroService.getAll();

                        if (danhSachPhong == null || danhSachPhong.isEmpty()) {
                            out.println("Hiện không có phòng nào trong hệ thống.");
                        } else {
                            StringBuilder sb = new StringBuilder("=== DANH SÁCH PHÒNG TRỌ ===\n");
                            for (PhongTro phong : danhSachPhong) {
                                sb.append("Mã phòng: ").append(phong.getMaPhong())
                                        .append(" | Tên: ").append(phong.getTenPhong())
                                        .append(" | Giá: ").append(phong.getGiaThue())
                                        .append(" | Trạng thái: ").append(phong.getTrangThai())
                                        .append("\n");
                            }
                            out.println(sb.toString().trim());
                        }
                    } catch (Exception dbEx) {
                        out.println("Lỗi truy xuất CSDL: " + dbEx.getMessage());
                    }
                } else if (command.toUpperCase().startsWith("BOOK ")) {
                    String roomCode = command.substring(5).trim();
                    try {
                        PhongTroService phongTroService = ApplicationContextProvider.getBean(PhongTroService.class);
                        PhongTro phong = phongTroService.getByMaPhong(roomCode);

                        if (phong == null) {
                            out.println("Thất bại! Không tìm thấy mã phòng: " + roomCode);
                        } else if (!"TRONG".equalsIgnoreCase(phong.getTrangThai())) {
                            out.println("Thất bại! Phòng " + roomCode + " không còn trống (Trạng thái: " + phong.getTrangThai() + ")");
                        } else {
                            phong.setTrangThai("DA_DAT");
                            phongTroService.update(phong.getId(), phong, null);
                            out.println("Thành công! Đã đặt giữ chỗ cho phòng: " + roomCode);
                        }
                    } catch (Exception dbEx) {
                        out.println("Lỗi khi xử lý đặt phòng: " + dbEx.getMessage());
                    }
                } else {
                    out.println("Lệnh không hợp lệ.");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi kết nối client: " + e.getMessage());
        } finally {
            System.out.println("Client đã ngắt kết nối. Giải phóng luồng: " + threadName);
        }
    }
}