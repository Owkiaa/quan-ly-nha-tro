package com.nhatro.socket;

import com.nhatro.service.DatChoMonitor;
import com.nhatro.service.DatChoService;
import com.nhatro.service.PhongTroService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Máy chủ Socket đa luồng sử dụng ExecutorService Thread Pool.
 * Tiêm trực tiếp DatChoService, PhongTroService và DatChoMonitor qua Spring DI để bảo toàn @Transactional, Pessimistic Lock và Monitor Chương 4.
 * Phụ trách bởi: Đoàn Văn Mẩn
 */
@Component
public class SocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private static final int THREAD_POOL_SIZE = 10;

    private final DatChoService datChoService;
    private final PhongTroService phongTroService;
    private final DatChoMonitor datChoMonitor;
    private final AtomicInteger clientCounter = new AtomicInteger(0);
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("client-" + clientCounter.incrementAndGet());
        return thread;
    });

    private volatile ServerSocket serverSocket;
    private volatile boolean running = true;

    @Autowired
    public SocketServer(DatChoService datChoService, PhongTroService phongTroService, DatChoMonitor datChoMonitor) {
        this.datChoService = datChoService;
        this.phongTroService = phongTroService;
        this.datChoMonitor = datChoMonitor;
    }

    public SocketServer(DatChoService datChoService, PhongTroService phongTroService) {
        this(datChoService, phongTroService, null);
    }

    public void startServer(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;
            log.info("[{}] Socket Server đang chạy tại port {} (Thread Pool {} luồng)",
                    Thread.currentThread().getName(), port, THREAD_POOL_SIZE);

            while (running && !ss.isClosed()) {
                try {
                    Socket clientSocket = ss.accept();
                    executorService.submit(new ClientHandler(clientSocket, datChoService, phongTroService, datChoMonitor));
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
