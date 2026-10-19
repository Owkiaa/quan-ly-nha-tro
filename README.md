# Hệ Thống Quản Lí Nhà Trọ

## 1. Giới thiệu đề tài
Đề tài đồ án môn **Công nghệ Java**: Xây dựng **Hệ thống quản lí nhà trọ**. Hệ thống cung cấp giải pháp số hóa toàn diện việc quản lý phòng trọ, khách thuê, hợp đồng, dịch vụ (điện, nước, internet), hóa đơn thu tiền hàng tháng và tích hợp Socket để giao tiếp giữa Client và Server.

## 2. Phiên bản JDK
- **Java SE Development Kit (JDK)**: `17 LTS` (Eclipse Adoptium Temurin 17 / OpenJDK 17)
- **Spring Boot**: `3.3.5`
- **Apache Maven**: `3.9+`

## 3. Yêu cầu cài đặt
1. **JDK 17**: Cài đặt và cấu hình biến môi trường `JAVA_HOME` trỏ đến thư mục cài JDK 17, đồng thời thêm `%JAVA_HOME%\bin` vào `PATH`.
2. **Apache Maven**: Đã được cài đặt và thêm vào biến môi trường `PATH`.
3. **Microsoft SQL Server**: Bản 2017/2019/2022 hoặc SQL Server Express, đã kích hoạt xác thực SQL Server (SQL Server and Windows Authentication mode), mở cổng `1433`.

## 4. Cách tạo cơ sở dữ liệu
1. Mở **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**.
2. Kết nối tới SQL Server instance của bạn.
3. Chạy lần lượt các file script trong thư mục `sql/`:
   - [sql/01_create_database.sql](sql/01_create_database.sql): Tạo cơ sở dữ liệu `NhaTroDB`.
   - [sql/02_schema_and_sample_data.sql](sql/02_schema_and_sample_data.sql): Khởi tạo các bảng và dữ liệu mẫu.

## 5. Cách chạy Server
1. Thiết lập biến môi trường mật khẩu cơ sở dữ liệu (nếu khác mật khẩu mặc định):
   - **Windows Command Prompt (cmd)**:
     ```cmd
     set DB_PASSWORD=your_actual_password
     ```
   - **Windows PowerShell**:
     ```powershell
     $env:DB_PASSWORD="your_actual_password"
     ```
2. Khởi chạy ứng dụng Spring Boot bằng Maven:
   ```bash
   mvn spring-boot:run
   ```
   Hoặc chạy file `NhaTroApplication.java` từ IntelliJ IDEA / Eclipse / VS Code.
3. Truy cập hệ thống trên trình duyệt:
   [http://localhost:8080](http://localhost:8080)

## 6. Cách chạy Client
Client là ứng dụng Java độc lập nằm trong thư mục `client/`:
1. Di chuyển vào thư mục `client/`:
   ```bash
   cd client
   ```
2. Biên dịch và chạy ứng dụng:
   ```bash
   javac -encoding UTF-8 -d bin src/com/nhatro/client/SocketClientApp.java
   java -cp bin com.nhatro.client.SocketClientApp
   ```
3. Xem hướng dẫn chi tiết tại [client/README.md](client/README.md).

## 7. Danh sách thành viên
| STT | Họ và Tên | MSSV | Vai trò / Nhiệm vụ |
|:---:|:---|:---:|:---|
| 1 | *Đang cập nhật* | *Đang cập nhật* | *Đang cập nhật* |
| 2 | *Đang cập nhật* | *Đang cập nhật* | *Đang cập nhật* |
| 3 | *Đang cập nhật* | *Đang cập nhật* | *Đang cập nhật* |
| 4 | *Đang cập nhật* | *Đang cập nhật* | *Đang cập nhật* |
