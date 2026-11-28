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
```

---

## 2. Bảng lệnh giao thức TCP

Ngay khi Client kết nối thành công, Server gửi 1 dòng chào mừng:
```text
OK#Ket noi thanh cong! Cac lenh ho tro: PING | LIST_ROOM | ROOM_DETAIL <ma_phong> | BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt> | EXIT
```

| Lệnh Client gửi | Mô tả | Phản hồi thành công | Phản hồi lỗi |
| :--- | :--- | :--- | :--- |
| `PING` | Kiểm tra kết nối Server | `PONG#Server online` | — |
| `LIST_ROOM` | Lấy danh sách phòng đang `TRONG` (đa dòng) | Dòng đầu: `OK#DANH SACH PHONG TRO TRONG (N)` (hoặc `OK#Hien khong co phong trong nao trong he thong.`)<br>Các dòng phòng: `Mã phòng: ... \| Tên: ... \| Loại: ... \| Diện tích: ...m2 \| Giá: ... \| Trạng thái: TRONG`<br>Dòng kết thúc: **`END_LIST_ROOM`** | `FAIL#Loi truy xuat CSDL: ...`<br>`END_LIST_ROOM` |
| `ROOM_DETAIL <ma_phong>` | Xem chi tiết thông tin phòng theo mã | `OK#CHI TIET PHONG <ma> \| Tên: ... \| Loại: ... \| Diện tích: ...m2 \| Giá thuê: ... \| Trạng thái: ... \| Mô tả: ...` | `FAIL#Thieu ma phong. Cu phap: ROOM_DETAIL <ma_phong>` hoặc `FAIL#Không tìm thấy phòng...` |
| `BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt>` | Đặt giữ chỗ phòng (khóa bi quan `PESSIMISTIC_WRITE`) | `OK#Dat cho thanh cong cho phong <ma_phong> (Ma phieu: #<id>)` | `FAIL#<ly_do>` (sai cú pháp, phòng đã đặt/thuê, tranh chấp đồng thời) |
| `BOOK <ma_phong>` | Cú pháp rút gọn dùng cho kiểm thử nhanh / `ConcurrencyBookingTest` | `OK#Dat cho thanh cong cho phong <ma_phong> (Ma phieu: #<id>)` | `FAIL#<ly_do>` |
| `EXIT` | Đóng phiên kết nối an toàn | `BYE#Tam biet` | — |
| *(Lệnh khác)* | Lệnh không hợp lệ | — | `FAIL#Lenh khong hop le. Ho tro: PING, LIST_ROOM, ROOM_DETAIL <ma_phong>, BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt>, EXIT` |

---

## 3. Quy ước đọc đa dòng (`LIST_ROOM`)

Thay vì dùng `Thread.sleep(50)` để chờ dữ liệu (dễ cắt cụt gói tin khi mạng chậm), giao thức sử dụng **sentinel line `END_LIST_ROOM`**:
- Server luôn gửi dòng `END_LIST_ROOM` sau khi kết thúc phản hồi `LIST_ROOM` (kể cả khi danh sách trống hoặc xảy ra lỗi CSDL).
- Client (`SocketClientApp` và `SocketClientSwingApp`) chỉ cần lặp `while ((line = in.readLine()) != null && !"END_LIST_ROOM".equals(line))` để nhận trọn vẹn dữ liệu.

---

## 4. Ví dụ phiên giao tiếp mẫu

```text
S -> OK#Ket noi thanh cong! Cac lenh ho tro: PING | LIST_ROOM | ROOM_DETAIL <ma_phong> | BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt> | EXIT
C -> PING
S -> PONG#Server online
C -> LIST_ROOM
S -> OK#DANH SACH PHONG TRO TRONG (2)
S -> Mã phòng: P101 | Tên: Phòng 101 | Loại: Đơn | Diện tích: 20.0m2 | Giá: 2200000 | Trạng thái: TRONG
S -> Mã phòng: P201 | Tên: Phòng 201 | Loại: Đơn | Diện tích: 22.0m2 | Giá: 2400000 | Trạng thái: TRONG
S -> END_LIST_ROOM
C -> ROOM_DETAIL P101
S -> OK#CHI TIET PHONG P101 | Tên: Phòng 101 | Loại: Đơn | Diện tích: 20.0m2 | Giá thuê: 2200000 | Trạng thái: TRONG | Mô tả: Phòng tầng 1 thoáng mát
C -> BOOK P101#Nguyen Van A#079204000123#0901234567
S -> OK#Dat cho thanh cong cho phong P101 (Ma phieu: #21)
C -> EXIT
S -> BYE#Tam biet
```

## 5. Xử lý đồng thời (Concurrency)

Khi nhiều Client cùng gửi `BOOK P101#...` tại cùng một thời điểm:
1. `ExecutorService` xử lý song song trên các luồng `client-1`, `client-2`, ...
2. `ClientHandler` gọi `datChoService.datChoPhong(...)` — truy vấn đầu tiên khóa bản ghi phòng bằng `PESSIMISTIC_WRITE` (`UPDLOCK` trên SQL Server).
3. Chỉ **duy nhất 1 Client** nhận `OK#Dat cho thanh cong...`; tất cả Client còn lại nhận `FAIL#...` (`RoomAlreadyBookedException`).
