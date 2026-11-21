package com.nhatro.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Chương trình Client tách biệt chạy bằng hàm main(),
 * dùng cho phần Socket (kết nối Client - Server) trong đồ án Công nghệ Java.
 */
public class SocketClientApp {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9999;

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        System.out.println("================================================");
        System.out.println("   CLIENT SOCKET - HỆ THỐNG QUẢN LÍ NHÀ TRỌ");
        System.out.println("================================================");
        System.out.println("Đang kết nối tới Socket Server tại " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            String greeting = in.readLine();
            if (greeting != null) {
                System.out.println("Server: " + greeting);
            }
            System.out.println("Cú pháp hỗ trợ: LIST_ROOM | BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt> | EXIT");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                String trimmed = userInput.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                out.println(trimmed);
                String serverResponse = in.readLine();
                if (serverResponse == null) {
                    System.out.println("Server đã đóng kết nối.");
                    break;
                }
                System.out.println("Server phản hồi:\n" + serverResponse);

                Thread.sleep(50);
                while (in.ready()) {
                    System.out.println(in.readLine());
                }

                if ("EXIT".equalsIgnoreCase(trimmed)) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể kết nối tới Socket Server tại " + host + ":" + port);
            System.err.println("Lưu ý: Hãy chắc chắn Server Socket đã được khởi động trước.");
            System.err.println("Chi tiết lỗi: " + e.getMessage());
        }
    }
}
