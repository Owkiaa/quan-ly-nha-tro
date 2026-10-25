package com.nhatro.socket; // Đổi theo đúng package của bạn

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

                System.out.println("Server phản hồi: " + in.readLine());
                if ("EXIT".equalsIgnoreCase(command)) break;
            }
        } catch (Exception e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
    }
}