package com.nhatro.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 9090);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            // Đọc câu chào từ Server
            System.out.println("Server: " + in.readLine());

            while (true) {
                System.out.print("Nhập lệnh: ");
                String command = scanner.nextLine();
                out.println(command); // Gửi lệnh lên Server

                // 1. Chờ và đọc dòng phản hồi đầu tiên
                String response = in.readLine();
                if (response == null) break;
                System.out.println("Server phản hồi:\n" + response);

                // 2. Đọc vét các dòng còn lại trong bộ đệm (dành cho kết quả nhiều dòng như LIST_ROOM)
                Thread.sleep(20); // Dừng một chút để TCP truyền đủ gói tin
                while (in.ready()) {
                    System.out.println(in.readLine());
                }

                if ("EXIT".equalsIgnoreCase(command)) break;
            }
        } catch (Exception e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
    }
}