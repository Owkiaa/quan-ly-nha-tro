package com.nhatro.socket;

import com.nhatro.service.DatChoService;
import com.nhatro.service.PhongTroService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Máy chủ Socket đa luồng sử dụng ExecutorService Thread Pool.
 * Tiêm trực tiếp DatChoService và PhongTroService qua Spring DI để bảo toàn @Transactional và Pessimistic Lock.
 * Phụ trách bởi: Đoàn Văn Mẩn
 */
@Component
public class SocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private static final int THREAD_POOL_SIZE = 10;

    private final DatChoService datChoService;
    private final PhongTroService phongTroService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private volatile ServerSocket serverSocket;
    private volatile boolean running = true;

    public SocketServer(DatChoService datChoService, PhongTroService phongTroService) {
        this.datChoService = datChoService;
        this.phongTroService = phongTroService;
    }

    public void startServer(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;
            log.info("[{}] Socket Server đang chạy tại port {} (Thread Pool {} luồng)",
                    Thread.currentThread().getName(), port, THREAD_POOL_SIZE);

            while (running && !ss.isClosed()) {
                try {
                    Socket clientSocket = ss.accept();
                    executorService.submit(new ClientHandler(clientSocket, datChoService, phongTroService));
                } catch (SocketException se) {
                    if (!running || ss.isClosed()) {
                        break;
                    }
                    log.error("[{}] Lỗi Socket khi tiếp nhận kết nối: {}",
                            Thread.currentThread().getName(), se.getMessage());
                }
            }
        } catch (BindException be) {
            log.warn("[{}] Cổng {} đã được sử dụng, bỏ qua khởi động Socket Server: {}",
                    Thread.currentThread().getName(), port, be.getMessage());
        } catch (IOException e) {
            log.error("[{}] Lỗi khởi động Socket Server ở port {}: {}",
                    Thread.currentThread().getName(), port, e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stopServer() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
        executorService.shutdownNow();
    }
}
