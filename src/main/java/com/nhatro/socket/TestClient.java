package com.nhatro.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", SocketRunner.SOCKET_PORT);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {

            // Đọc câu chào từ Server
            System.out.println("Server: " + in.readLine());

            while (true) {
                System.out.print("Nhập lệnh: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String command = scanner.nextLine();
                out.println(command);

                // 1. Chờ và đọc dòng phản hồi đầu tiên
                String response = in.readLine();
                if (response == null) {
                    break;
                }
                System.out.println("Server phản hồi:\n" + response);

                // 2. Đọc vét các dòng còn lại trong bộ đệm (dành cho kết quả nhiều dòng như LIST_ROOM)
                Thread.sleep(50);
                while (in.ready()) {
                    System.out.println(in.readLine());
                }

                if ("EXIT".equalsIgnoreCase(command.trim())) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
    }
}
