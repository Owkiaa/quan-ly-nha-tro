# Đặc tả giao thức Socket — Hệ thống Quản lý Nhà trọ

- **Phụ trách:** Đoàn Văn Mẩn (TV4)
- **Nhánh làm việc:** `feature/man-upgrade`
- **Port mặc định của Server (`SocketRunner.SOCKET_PORT`):** `9999`
- **Bảng mã:** UTF-8, mỗi thông điệp kết thúc bằng ký tự xuống dòng (`\n`, đọc bằng `BufferedReader.readLine()`).

---

## 1. Kiến trúc đa luồng (`ExecutorService`)

`SocketRunner` khởi chạy `SocketServer.startServer(SOCKET_PORT)` trên một luồng daemon `socket-server-acceptor`. Mỗi khi có kết nối mới, `ServerSocket.accept()` chấp nhận `Socket` và đẩy vào `ExecutorService` (`Executors.newFixedThreadPool(10, ...)`), gán tên luồng `client-1`, `client-2`, ...

```text
                        +-----------------------------------+
                        |       SocketRunner (Daemon)       |
                        |     thread: socket-server-acceptor|
                        +-----------------+-----------------+
                                          |
                                          v
                        +-----------------------------------+
                        |   SocketServer (port 9999)        |
                        |   ServerSocket.accept()           |
                        +-----------------+-----------------+
                                          | submit(ClientHandler)
                                          v
         +-----------------------------------------------------------------+
         |              ExecutorService (FixedThreadPool = 10)             |
         |  +-----------------------+  +-----------------------+           |
         |  |   Thread: client-1    |  |   Thread: client-2    |  ...    |
         |  |   ClientHandler       |  |   ClientHandler       |           |
         |  +-----------+-----------+  +-----------+-----------+           |
         +--------------|----------------------------|---------------------+
                        |                            |
                        v                            v
         +-----------------------------------------------------------------+
         |   DatChoService, PhongTroService, DatChoMonitor (@Transactional) |
         +-----------------------------------------------------------------+