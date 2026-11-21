package com.nhatro.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Khởi động Socket Server ngầm cùng vòng đời ứng dụng Spring Boot.
 * Phụ trách bởi: Đoàn Văn Mẩn
 */
@Component
public class SocketRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SocketRunner.class);
    public static final int SOCKET_PORT = 9999;

    private final SocketServer socketServer;

    public SocketRunner(SocketServer socketServer) {
        this.socketServer = socketServer;
    }

    @Override
    public void run(String... args) {
        log.info("Đang khởi động mạng Socket Server ngầm tại port {}...", SOCKET_PORT);
        Thread serverThread = new Thread(() -> socketServer.startServer(SOCKET_PORT), "socket-server-acceptor");
        serverThread.setDaemon(true);
        serverThread.start();
    }
}
