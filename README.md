# Hệ Thống Quản Lí Nhà Trọ

## 1. Giới thiệu đề tài
Đề tài đồ án môn **Công nghệ Java**: Xây dựng **Hệ thống quản lí nhà trọ**. Hệ thống cung cấp giải pháp số hóa toàn diện việc quản lý phòng trọ, khách thuê, hợp đồng, dịch vụ (điện, nước, internet), hóa đơn thu tiền hàng tháng và tích hợp Socket để giao tiếp giữa Client và Server.

## 2. Công nghệ & Phiên bản sử dụng
- **Java SE Development Kit (JDK)**: `17 LTS` (Eclipse Adoptium Temurin 17 / OpenJDK 17)
- **Framework**: `Spring Boot 3.3.5` (Spring Web, Thymeleaf, Spring Data JPA)
- **Database**: `MySQL 8.0+` (Driver: `com.mysql:mysql-connector-j`)
- **Frontend UI**: `Thymeleaf Template Engine` kết hợp `Bootstrap 5.3 CDN`
- **Socket**: Java Networking (`java.net.Socket`, `ServerSocket`) độc lập phục vụ đa client
- **Build Tool**: `Apache Maven 3.9+` (hoặc Maven Wrapper đi kèm `mvnw`)

## 3. Yêu cầu cài đặt
1. **JDK 17**: Cài đặt và cấu hình biến môi trường `JAVA_HOME` trỏ đến thư mục cài JDK 17, đồng thời thêm `%JAVA_HOME%\bin` vào `PATH`.
2. **MySQL Server 8.0+**: Đang chạy dịch vụ MySQL trên cổng `3306`.
3. **Apache Maven**: Đã được cài đặt hoặc sử dụng trực tiếp `./mvnw` / `mvnw.cmd` đi kèm dự án.

## 4. Cách tạo cơ sở dữ liệu `nhatro_db`
1. Mở **MySQL Workbench**, **phpMyAdmin**, hoặc **MySQL Command Line Client**.
2. Đăng nhập vào MySQL Server của bạn (tài khoản `root` hoặc tài khoản có quyền tạo CSDL).
3. Chạy lần lượt các file script trong thư mục `sql/`:
   - [sql/01_create_database.sql](sql/01_create_database.sql): Tạo cơ sở dữ liệu `nhatro_db` với bảng mã `utf8mb4`.
   - [sql/02_schema_and_sample_data.sql](sql/02_schema_and_sample_data.sql): Khởi tạo các bảng và dữ liệu mẫu ban đầu.
   
   *Hoặc thực thi nhanh qua dòng lệnh:*
   ```bash
   mysql -u root -p < sql/01_create_database.sql
   mysql -u root -p nhatro_db < sql/02_schema_and_sample_data.sql
   ```

## 5. Cách chạy Server
1. Thiết lập biến môi trường mật khẩu cơ sở dữ liệu (để bảo mật, không commit mật khẩu thật lên Git):
   - **Windows Command Prompt (cmd)**:
     ```cmd
     set DB_PASSWORD=your_actual_password
     ```
   - **Windows PowerShell**:
     ```powershell
     $env:DB_PASSWORD="your_actual_password"
     ```
   *(Nếu bạn dùng username khác `root`, có thể thiết lập thêm biến môi trường `$env:DB_USERNAME="your_username"`).*

2. Khởi chạy ứng dụng Spring Boot bằng Maven Wrapper:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
   Hoặc chạy file `NhaTroApplication.java` trực tiếp từ IntelliJ IDEA / Eclipse / VS Code.

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
