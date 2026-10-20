# Hệ Thống Quản Lí Nhà Trọ

## 1. Giới thiệu đề tài
Đề tài đồ án môn **Công nghệ Java**: Xây dựng **Hệ thống quản lí nhà trọ**. Hệ thống cung cấp giải pháp số hóa toàn diện việc quản lý phòng trọ, khách thuê, đặt giữ chỗ, hợp đồng thuê phòng, dịch vụ điện nước, hóa đơn định kỳ, tiếp nhận phản ánh sửa chữa cơ sở vật chất, tích hợp Socket Server đa client và cơ chế xử lý đồng thời chống thuê trùng phòng.

## 2. Công nghệ & Phiên bản sử dụng
- **Java SE Development Kit (JDK)**: `Java 17 LTS` (Eclipse Adoptium Temurin 17 / OpenJDK 17)
- **Framework**: `Spring Boot 3.3.5` (Spring Web, Thymeleaf, Spring Data JPA)
- **Database**: `Microsoft SQL Server` (Driver: `com.microsoft.sqlserver:mssql-jdbc`)
- **Frontend UI**: `Thymeleaf Template Engine` kết hợp `Bootstrap 5.3 CDN`
- **Socket**: Java Networking (`java.net.Socket`, `ServerSocket`) độc lập phục vụ tối thiểu 3 client kết nối cùng lúc
- **Build Tool**: `Apache Maven 3.9+` (hoặc Maven Wrapper đi kèm `mvnw.cmd`)
- **Không dùng Lombok**: Toàn bộ Entity, DTO, Repository viết thủ công constructor, getter, setter.

## 3. Yêu cầu cài đặt môi trường
1. **JDK 17**: Cài đặt JDK 17, thiết lập biến môi trường `JAVA_HOME` và thêm `%JAVA_HOME%\bin` vào `PATH`.
2. **Microsoft SQL Server**: Bản 2017/2019/2022 hoặc SQL Server Express, đã bật xác thực SQL Server (SQL Server and Windows Authentication mode), mở cổng mặc định `1433`.
3. **Apache Maven**: Sử dụng trực tiếp Maven Wrapper (`.\mvnw.cmd` trên Windows hoặc `./mvnw` trên Linux/macOS).

## 4. Cách tạo cơ sở dữ liệu `NhaTroDB`
1. Mở **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**.
2. Kết nối tới SQL Server instance của bạn (tài khoản `sa` hoặc tài khoản có quyền sysadmin).
3. Mở và thực thi lần lượt 2 file script trong thư mục [sql/](sql/):
   - [sql/01_create_database.sql](sql/01_create_database.sql): Tạo cơ sở dữ liệu `NhaTroDB`.
   - [sql/02_schema_and_sample_data.sql](sql/02_schema_and_sample_data.sql): Tạo 7 bảng dữ liệu và chèn 20 bản ghi mẫu nhất quán logic.
   - [sql/reset_p201.sql](sql/reset_p201.sql): Script dọn dẹp đặt chỗ và đưa phòng P201 về `TRONG` trước mỗi lần chạy kiểm thử đồng thời (Concurrency Test).

## 5. Danh sách tài khoản mẫu trong hệ thống
Mật khẩu trong bảng `tai_khoan` được lưu dưới dạng chuỗi băm chuẩn `salt:hash` (16 ký tự hex salt + SHA-256 hex của `salt + password`):

| Tên đăng nhập | Mật khẩu mẫu | Vai trò (`vai_tro`) | Hồ sơ khách liên kết | Ghi chú |
|:---|:---|:---:|:---:|:---|
| `admin` | `Admin@123` | `ADMIN` | *Không* | Quản trị viên cao nhất của hệ thống |
| `staff1` | `Staff@123` | `STAFF` | *Không* | Nhân viên quản lý nhà trọ 1 |
| `staff2` | `Staff@123` | `STAFF` | *Không* | Nhân viên quản lý nhà trọ 2 |
| `tenant01` | `Tenant@123` | `TENANT` | Khách 1 (Nguyễn Văn An) | Khách thuê phòng |
| `tenant02` | `Tenant@123` | `TENANT` | Khách 2 (Trần Thị Bích) | Khách thuê phòng |
| `tenant03` | `Tenant@123` | `TENANT` | Khách 3 (Lê Hoàng Cường) | Khách thuê phòng |
| `tenant04` đến `tenant17` | `Tenant@123` | `TENANT` | Khách 4 đến 17 | Tương ứng từng hồ sơ khách thuê |

## 6. Cách chạy Server
1. Thiết lập biến môi trường mật khẩu cơ sở dữ liệu (không lưu mật khẩu thật trong mã nguồn):
   - **Windows Command Prompt (cmd)**:
     ```cmd
     set DB_PASSWORD=your_actual_password
     ```
   - **Windows PowerShell**:
     ```powershell
     $env:DB_PASSWORD="your_actual_password"
     ```
   *(Nếu tài khoản đăng nhập khác `sa`, có thể thiết lập thêm `$env:DB_USERNAME="your_username"`).*

2. Khởi chạy ứng dụng Spring Boot bằng Maven Wrapper:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   Hoặc chạy hàm `main()` trong file [NhaTroApplication.java](src/main/java/com/nhatro/NhaTroApplication.java) từ IDE (IntelliJ IDEA / Eclipse / VS Code).

3. Truy cập hệ thống trên trình duyệt:
   [http://localhost:8080](http://localhost:8080)

## 7. Cách chạy Desktop Client
*Đang cập nhật (Module của Đoàn Văn Mẩn trong Tuần 4).*

## 8. Danh sách thành viên nhóm (5 thành viên)
| STT | Họ và Tên | MSSV | Vai trò & Mảng phụ trách chính |
|:---:|:---|:---:|:---|
| 1 | **Nguyễn Minh Tâm** | 2001240423 | CSDL, Kiến trúc tổng thể, CRUD Phòng trọ, Khách thuê, Tài khoản & Xác thực |
| 2 | **Bùi Trần Tấn Tài** | 2001240413 | Nghiệp vụ Hợp đồng, Đặt chỗ (`dat_cho`), Khóa đồng thời `findByIdForUpdate`, Tác vụ nền `@Scheduled` |
| 3 | **Lưu Quốc Trụ** | 2001240521 | Khung Layout, Bộ Thymeleaf Fragment dùng lại (Reusable Components), Cổng Khách thuê (Tenant Portal) |
| 4 | **Đoàn Văn Mẩn** | 2001240270 | Mạng Socket (Multi-client), Desktop Client độc lập, Quản lý sự cố, Kiểm thử `ConcurrencyBookingTest` |
| 5 | **Trương Đỗ Minh Quân** | 2001240390 | Chỉ số Điện - Nước, Lập hóa đơn định kỳ, Màn hình thu ngân, In hóa đơn & Dashboard thống kê |
