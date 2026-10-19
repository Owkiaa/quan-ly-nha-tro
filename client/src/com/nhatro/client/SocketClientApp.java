package com.nhatro.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
        System.out.println("   KHUNG CLIENT SOCKET - HỆ THỐNG QUẢN LÍ NHÀ TRỌ");
        System.out.println("================================================");
        System.out.println("Đang kết nối tới Socket Server tại " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Kết nối Socket thành công! (Nhập 'exit' để thoát)");
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if ("exit".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("Đang ngắt kết nối...");
                    break;
                }
                out.println(userInput);
                String serverResponse = in.readLine();
                System.out.println("Server phản hồi: " + serverResponse);
            }
        } catch (Exception e) {
            System.err.println("Không thể kết nối tới Socket Server tại " + host + ":" + port);
            System.err.println("Lưu ý: Hãy chắc chắn Server Socket đã được khởi động trước.");
            System.err.println("Chi tiết lỗi: " + e.getMessage());
        }
    }
}
