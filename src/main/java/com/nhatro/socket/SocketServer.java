package com.nhatro.socket; // Đổi lại theo đúng tên package gốc dự án của bạn

import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Socket Server đang chạy ở port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Tạo luồng mới cho mỗi client kết nối
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}