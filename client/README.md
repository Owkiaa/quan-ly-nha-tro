# Chương Trình Desktop Client Socket — Quản Lý Nhà Trọ

Thư mục `client/` chứa 2 ứng dụng Java độc lập (thuần JDK 17, không phụ thuộc Spring Boot) kết nối trực tiếp tới Socket Server cổng `9999`:

1. **`SocketClientApp.java`** — Giao diện dòng lệnh (Console) có menu chọn số (`1–4`, `0` thoát) và kiểm tra định dạng CCCD/SĐT.
2. **`SocketClientSwingApp.java`** — Giao diện đồ họa Desktop (Java Swing) có bảng phòng trống `JTable`, click chọn phòng tự điền form giữ chỗ và khung log gói tin thời gian thực.

Chi tiết giao thức và bảng lệnh xem tại [docs/SOCKET_PROTOCOL.md](../docs/SOCKET_PROTOCOL.md).

---

## 1. Biên dịch mã nguồn

Từ thư mục gốc của dự án:

```bash
mkdir -p client/out
javac -encoding UTF-8 -d client/out client/src/com/nhatro/client/*.java
```

*(Trên Windows PowerShell:)*
```powershell
New-Item -ItemType Directory -Force -Path client\out | Out-Null
javac -encoding UTF-8 -d client\out client\src\com\nhatro\client\*.java
```

---

## 2. Chạy Client

> **Lưu ý:** Đảm bảo Spring Boot Server (`mvn spring-boot:run`) đang chạy để mở cổng Socket `9999`.

### Cách A — Chạy bản Console (`SocketClientApp`)
```bash
java -cp client/out com.nhatro.client.SocketClientApp
# Hoặc chỉ định Host/Port:
java -cp client/out com.nhatro.client.SocketClientApp localhost 9999
```

### Cách B — Chạy bản Desktop Swing (`SocketClientSwingApp`)
```bash
java -cp client/out com.nhatro.client.SocketClientSwingApp
```
- Nhấn **Kết nối** (mặc định `localhost:9999`) → **Tải phòng trống** để nạp dữ liệu lên bảng `JTable`.
- Click một dòng phòng bất kỳ để tự động điền **Mã phòng**, nhập Họ tên / CCCD (9 hoặc 12 số) / SĐT (`0xxxxxxxxx`) → nhấn **Đặt giữ chỗ**.

---

## 3. Hướng dẫn demo đa luồng nhiều Client cùng đặt 1 phòng

1. Mở 2–3 cửa sổ `SocketClientSwingApp` (hoặc `SocketClientApp`) cùng kết nối tới `localhost:9999`.
2. Trên cả 2 cửa sổ, cùng chọn 1 phòng đang `TRONG` (ví dụ `P101`) và điền thông tin khách thuê khác nhau.
3. Nhấn **Đặt giữ chỗ** gần như đồng thời:
   - Đúng **1 Client** nhận phản hồi `OK#Dat cho thanh cong...`.
   - Các Client còn lại nhận `FAIL#...` nhờ cơ chế khóa bi quan (`PESSIMISTIC_WRITE`) và luồng xử lý song song `client-1`, `client-2` trên Server.
4. Hoặc chạy bài test đa luồng tự động trên Server (`ConcurrencyBookingTest`).
