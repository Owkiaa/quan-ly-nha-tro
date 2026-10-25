package com.nhatro.socket; // Sửa lại nếu package của bạn khác

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SocketRunner implements CommandLineRunner {

    // Đặt port khác với port của Web (thường là 8080)
    private static final int SOCKET_PORT = 9090;

    @Override
    public void run(String... args) {
        System.out.println("Đang khởi động mạng Socket Server ngầm...");
        new Thread(() -> {
            SocketServer server = new SocketServer();
            server.startServer(SOCKET_PORT);
        }).start();
    }
}