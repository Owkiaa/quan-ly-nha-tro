# Chương Trình Client Socket - Quản Lí Nhà Trọ

Đây là ứng dụng Client Java độc lập (chạy hàm `main`), dùng để kết nối Socket với Server cho đồ án Công nghệ Java.

## Cách biên dịch và chạy:

1. Di chuyển vào thư mục `client`:
   ```bash
   cd client
   ```
2. Biên dịch mã nguồn:
   ```bash
   javac -encoding UTF-8 -d bin src/com/nhatro/client/SocketClientApp.java
   ```
3. Chạy client:
   ```bash
   java -cp bin com.nhatro.client.SocketClientApp
   ```
   Hoặc chỉ định Host và Port tùy ý:
   ```bash
   java -cp bin com.nhatro.client.SocketClientApp localhost 9999
   ```
