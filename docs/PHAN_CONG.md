# KẾ HOẠCH VÀ BẢNG PHÂN CÔNG ĐỒ ÁN MÔN CÔNG NGHỆ JAVA

**Đề tài:** Hệ Thống Quản Lí Nhà Trọ  
**Công nghệ:** Spring Boot 3.3.5, Spring Data JPA, Thymeleaf, Microsoft SQL Server, Bootstrap 5.3 (CDN), Java Socket  
**Nhánh Git làm việc chính:** `dev`  
**Hạn nộp dự kiến:** **[Chờ xác nhận từ giảng viên]**  

---

## I. THÔNG TIN NHÓM VÀ BẢNG PHÂN CÔNG NHIỆM VỤ CHÍNH

Nhóm gồm **4 thành viên**. Trách nhiệm được phân bổ đồng đều giữa 4 thành viên theo các mảng kiến trúc và nghiệp vụ chuyên sâu. Khi vấn đáp, từng thành viên **phải tự trình bày và giải thích cặn kẽ toàn bộ phần mã nguồn mình đã thực hiện**.

| STT | Họ và Tên | MSSV | Mảng phụ trách chính | Phạm vi trách nhiệm & Vấn đáp |
|:---:|:---|:---:|:---|:---|
| **1** | **Nguyễn Minh Tâm** *(Trưởng nhóm)* | 2001240423 | **Cơ sở dữ liệu, Kiến trúc tổng thể, Phòng trọ, Khách thuê, Tài khoản & Xác thực** | - Thiết kế ERD, Schema CSDL SQL Server (`NhaTroDB`), viết script SQL tạo database và dữ liệu mẫu.<br>- Hoàn thành và đẩy các Entity & Repository thủ công lên nhánh `dev` trong 2–3 ngày đầu của Tuần 1 để unblock nhóm.<br>- Hiện thực trọn gói (Controller, Service, View Thymeleaf) cho danh mục Phòng trọ (kèm ảnh), Khách thuê.<br>- Quản lý tài khoản, Đăng nhập và Phân quyền bằng `HttpSession` & `HandlerInterceptor` (không dùng Spring Security).<br>- Mã hóa mật khẩu bằng SHA-256 kèm salt (`java.security.MessageDigest`).<br>- Quản lý cấu trúc dự án và điều phối Git. |
| **2** | **[Họ tên Thành viên 2]** | [MSSV TV2] | **Nghiệp vụ Hợp đồng, Đặt chỗ, Hóa đơn, Điện Nước, Xử lý ĐỒNG THỜI & Tác vụ nền @Scheduled** | - Hiện thực trọn gói (Controller, Service, View Thymeleaf) cho Hợp đồng thuê phòng, Đặt chỗ (`dat_cho`), Chỉ số Điện - Nước, Hóa đơn định kỳ.<br>- Ràng buộc toàn vẹn khi lập/thanh lý hợp đồng và gạch nợ hóa đơn (`UNIQUE(hop_dong_id, thang_nam)`).<br>- **Thiết kế và hiện thực cơ chế XỬ LÝ ĐỒNG THỜI (Concurrency Lock)** bằng `findByIdForUpdate` (Pessimistic Write Lock) chống thuê/đặt trùng phòng.<br>- **Hiện thực tác vụ nền `@Scheduled`** định kỳ quét đặt chỗ quá hạn để tự động hủy và trả phòng về `TRONG` (phần đa luồng thứ hai).<br>- Giải thích cơ chế Transaction, Isolation Level, Lock và Background Task khi vấn đáp. |
| **3** | **[Họ tên Thành viên 3]** | [MSSV TV3] | **Kiến trúc Giao diện Thymeleaf, Layouts, Bootstrap CDN & Cổng Khách thuê (Tenant Portal)** | - Xây dựng khung giao diện chuẩn (Sidebar, Navbar, Footer) bằng Thymeleaf Layout Fragments.<br>- Tích hợp Bootstrap 5.3 CDN và CSS Responsive (Desktop/Mobile), màu sắc hài hòa, trực quan.<br>- Chuẩn hóa UX: tìm kiếm, phân trang, sắp xếp, hộp xác nhận xóa, thông báo lỗi cạnh ô nhập và flash message.<br>- Hiện thực toàn bộ các trang giao diện cho Khách thuê: Tra cứu phòng trống, Xem chi tiết phòng, Xem thông tin hợp đồng, Tra cứu hóa đơn, Gửi phiếu phản ánh sự cố. |
| **4** | **[Họ tên Thành viên 4]** | [MSSV TV4] | **Mạng Socket (Đa Client), Quản lý Sự cố, Dashboard Thống kê & Bộ Kiểm thử Độc lập** | - Hiện thực Socket Server đa luồng (`ServerSocket`, `ExecutorService`) phục vụ tối thiểu **3 Client kết nối cùng lúc**.<br>- Cấu hình Socket Server khởi động cùng ứng dụng qua `CommandLineRunner`; tiêm Service vào `ClientHandler` qua Dependency Injection (không dùng `new`) để đảm bảo `@Transactional` và `findByIdForUpdate` hoạt động.<br>- Xây dựng ứng dụng Desktop Client độc lập (`SocketClientApp.java`) dùng try-with-resources.<br>- Hiện thực trọn gói (Backend Service, Controller, View Thymeleaf, Biểu đồ) cho Dashboard thống kê doanh thu và công suất phòng.<br>- Hiện thực trọn gói module Quản lý sự cố / Yêu cầu sửa chữa (phân công thợ, cập nhật chi phí).<br>- Viết chương trình kiểm thử đồng thời độc lập `ConcurrencyBookingTest.java` (Java Core, Socket, `CountDownLatch`). |

---

## II. DANH SÁCH CHỨC NĂNG HỆ THỐNG VÀ QUY TẮC PHÂN CHIA

### 1. Quy tắc phân công phát triển mã nguồn
- **Mỗi thành viên tự viết trọn gói Controller, Service và template Thymeleaf cho chức năng mình phụ trách** (mô hình MVC hoàn chỉnh theo từng module nghiệp vụ).
- **Thành viên 3** chịu trách nhiệm thiết lập nền tảng giao diện: Layout chính, Fragment chung, CSS toàn cục, Bootstrap 5.3 CDN và toàn bộ các trang màn hình dành cho Khách thuê.
- **Thành viên 4** phụ trách thống nhất toàn bộ Dashboard thống kê: trực tiếp viết từ tầng truy vấn dữ liệu, Service tính toán số liệu đến Controller và trang hiển thị biểu đồ Thymeleaf.

---

### 2. Danh sách 16 chức năng chi tiết (Mỗi chức năng 1 người chịu trách nhiệm chính)

#### Phân hệ 1: Quản trị danh mục & Xác thực (Thành viên 1 phụ trách chính)
1. **Quản lý danh mục phòng trọ (CRUD):** Thêm phòng mới, cập nhật diện tích, đơn giá, loại phòng, ảnh đại diện (`hinh_anh`), mô tả; cập nhật trạng thái phòng.  
   *👉 Người phụ trách chính: Nguyễn Minh Tâm*
2. **Quản lý hồ sơ khách thuê (CRUD):** Tiếp nhận thông tin định danh khách thuê (Họ tên, CCCD, SĐT, Email, Quê quán, Ngày sinh, Biển số xe).  
   *👉 Người phụ trách chính: Nguyễn Minh Tâm*
3. **Quản lý tài khoản & Phân quyền truy cập:** Đăng ký, đổi mật khẩu, đăng nhập hệ thống, kiểm tra quyền truy cập theo vai trò (`ADMIN`, `STAFF`, `TENANT`) bằng `HttpSession` và `HandlerInterceptor`.  
   *👉 Người phụ trách chính: Nguyễn Minh Tâm*
4. **Mã hóa và xác thực mật khẩu:** Sử dụng thuật toán SHA-256 kèm chuỗi muối (salt) thông qua thư viện chuẩn `java.security.MessageDigest`, không dùng Spring Security hay BCrypt. Dữ liệu mẫu lưu mật khẩu đã băm.  
   *👉 Người phụ trách chính: Nguyễn Minh Tâm*

#### Phân hệ 2: Hợp đồng, Đặt chỗ, Điện Nước & Xử lý Đồng thời (Thành viên 2 phụ trách chính)
5. **Quản lý đặt giữ chỗ phòng (`dat_cho`):** Khách đặt giữ chỗ phòng qua Web hoặc Socket; ghi nhận thời hạn giữ chỗ (`han_giu_cho`). Khi đặt giữ chỗ thành công, chuyển phòng sang trạng thái `DA_DAT`.  
   *👉 Người phụ trách chính: [Thành viên 2]*
6. **Quản lý hợp đồng thuê phòng:** Tạo hợp đồng thuê, quy định tiền cọc, ngày bắt đầu - kết thúc, chu kỳ đóng tiền, gia hạn hoặc thanh lý hợp đồng. Chỉ cho phép tạo hợp đồng với phòng đang `TRONG`, hoặc phòng đang `DA_DAT` nếu người ký hợp đồng đúng là khách đã giữ chỗ trong `dat_cho` (trạng thái `DANG_GIU`). Khi tạo xong, chuyển đặt chỗ sang `DA_CHUYEN_HOP_DONG` và phòng sang `DA_THUE`.  
   *👉 Người phụ trách chính: [Thành viên 2]*
7. **Quản lý chỉ số điện - nước:** Chốt chỉ số điện nước đầu kỳ và cuối kỳ cho từng phòng theo tháng; tự động tính lượng tiêu thụ thực tế.  
   *👉 Người phụ trách chính: [Thành viên 2]*
8. **Lập và quản lý hóa đơn tiền phòng hàng tháng:** Tự động tổng hợp tiền phòng, tiền điện, tiền nước, phí dịch vụ; kiểm soát ràng buộc duy nhất `UNIQUE(hop_dong_id, thang_nam)`; quản lý trạng thái thanh toán và gạch nợ.  
   *👉 Người phụ trách chính: [Thành viên 2]*
9. **Cơ chế khóa đồng thời chống thuê/đặt trùng phòng:** Hiện thực hàm `findByIdForUpdate` trong Repository kết hợp `@Transactional` để khóa dòng dữ liệu phòng trọ, dùng chung cho cả luồng Web và luồng Socket.  
   *👉 Người phụ trách chính: [Thành viên 2]*
10. **Tác vụ nền tự động giải phóng chỗ quá hạn (@Scheduled):** Tác vụ nền quét định kỳ bảng `dat_cho`; nếu bản ghi đang `DANG_GIU` mà quá hạn (`han_giu_cho < GETDATE()`), tự động chuyển trạng thái đặt chỗ thành `HET_HAN` và đưa phòng từ `DA_DAT` về `TRONG`.  
    *👉 Người phụ trách chính: [Thành viên 2]*

#### Phân hệ 3: Giao diện nền tảng & Cổng Khách thuê (Thành viên 3 phụ trách chính)
11. **Khung giao diện dùng chung & Điều hướng phân quyền:** Xây dựng Sidebar, Navbar, Footer, Header responsive bằng Bootstrap 5.3 CDN và Thymeleaf Layout Fragments; hiển thị menu tương ứng theo Session vai trò.  
    *👉 Người phụ trách chính: [Thành viên 3]*
12. **Cổng tra cứu phòng trống & Xem chi tiết phòng trực tuyến:** Dành cho khách xem danh sách phòng trống, lọc theo giá, diện tích, xem ảnh phòng trực quan.  
    *👉 Người phụ trách chính: [Thành viên 3]*
13. **Cổng thông tin khách thuê trực tuyến:** Khách thuê đăng nhập xem thông tin hợp đồng hiện tại và tra cứu chi tiết hóa đơn các tháng.  
    *👉 Người phụ trách chính: [Thành viên 3]*
14. **Giao diện gửi phản ánh / yêu cầu sửa chữa:** Form trực tuyến để khách thuê gửi yêu cầu hỏng hóc thiết bị, chọn mức độ khẩn cấp kèm mô tả chi tiết.  
    *👉 Người phụ trách chính: [Thành viên 3]*

#### Phân hệ 4: Socket Đa Client, Sự cố, Dashboard & Kiểm thử (Thành viên 4 phụ trách chính)
15. **Quản lý và điều phối yêu cầu sửa chữa cơ sở vật chất:** Tiếp nhận phiếu phản ánh từ khách thuê, phân công thợ sửa chữa (`tho_sua_chua`), cập nhật chi phí khắc phục (`chi_phi`) và tiến độ xử lý.  
    *👉 Người phụ trách chính: [Thành viên 4]*
16. **Dashboard thống kê doanh thu & Báo cáo tổng quan (Backend + View):** Tự viết tầng Service truy vấn số liệu và giao diện Thymeleaf hiển thị biểu đồ doanh thu theo tháng, tỷ lệ phòng trống/đang thuê, nợ đọng tiền phòng.  
    *👉 Người phụ trách chính: [Thành viên 4]*
17. **Máy chủ Socket Server đa luồng (Multi-client Socket Server):** Lắng nghe cổng riêng (Port 9999), quản lý kết nối bằng Thread Pool (`ExecutorService`), phục vụ tối thiểu 3 Client kết nối cùng lúc; khởi động qua `CommandLineRunner`, nhận Service qua Dependency Injection; xử lý lệnh `BOOK` gọi `findByIdForUpdate`.  
    *👉 Người phụ trách chính: [Thành viên 4]*
18. **Ứng dụng Desktop Client độc lập & Chương trình kiểm thử Concurrency:** Ứng dụng Client độc lập dùng cú pháp try-with-resources và chương trình kiểm thử đồng thời độc lập `ConcurrencyBookingTest.java` (mở 10 luồng gửi `BOOK P201` qua Socket, dùng `CountDownLatch`, đúng 1 luồng OK và 9 luồng bị từ chối).  
    *👉 Người phụ trách chính: [Thành viên 4]*

---

### 3. Quy chuẩn giao diện bắt buộc cho toàn bộ các trang danh sách
Mọi màn hình danh sách (Phòng trọ, Khách thuê, Hợp đồng, Hóa đơn, Sự cố) đều phải tuân thủ nghiêm ngặt 4 tiêu chuẩn sau:
- **Tìm kiếm, Phân trang, Sắp xếp (Search, Pagination, Sort):** Hỗ trợ tìm kiếm từ khóa, lọc theo trạng thái, phân trang bằng `Pageable` của Spring Data JPA, sắp xếp linh hoạt theo các cột dữ liệu.
- **Hộp thoại xác nhận trước khi xóa (Confirm Dialog):** Modal xác nhận trước khi thực hiện hành động xóa để ngăn chặn việc người dùng thao tác nhầm lẫn.
- **Thông báo lỗi cạnh ô nhập (Inline Field Errors):** Khi dữ liệu nhập không hợp lệ, thông báo lỗi màu đỏ phải hiển thị ngay bên dưới ô nhập liệu tương ứng.
- **Thông báo thành công sau khi lưu (Flash Message):** Sau mỗi thao tác Thêm / Sửa / Xóa thành công, hiển thị Alert thông báo màu xanh lá trên đầu trang qua `RedirectAttributes`.

---

### 4. Quy tắc ràng buộc toàn vẹn khi xóa dữ liệu (Delete Integrity Rules)
Hệ thống phải kiểm tra nghiệp vụ và chặn xóa dữ liệu có ràng buộc, hiển thị thông báo lỗi thân thiện thay vì để văng lỗi cơ sở dữ liệu:
- **Chặn xóa phòng trọ:** Không cho phép xóa phòng đang có hợp đồng (`hop_dong`) hoặc đang có yêu cầu sửa chữa chưa xử lý xong. Thông báo: *"Không thể xóa phòng [Mã phòng] do đang có hợp đồng hoặc lịch sử sửa chữa liên kết. Vui lòng kiểm tra lại."*
- **Chặn xóa khách thuê:** Không cho phép xóa khách thuê đang đứng tên hợp đồng thuê phòng hoặc đang có tài khoản liên kết. Thông báo: *"Không thể xóa khách thuê do đang có hợp đồng thuê phòng hiệu lực."*
- **Chặn xóa hợp đồng:** Không cho phép xóa hợp đồng nếu đã phát sinh hóa đơn tiền phòng (`hoa_don`). Thông báo: *"Không thể xóa hợp đồng đã phát sinh hóa đơn thu tiền. Vui lòng chuyển trạng thái sang Đã thanh lý."*

---

### 5. Thống nhất 4 trạng thái chuẩn của Phòng trọ và Luồng nghiệp vụ
**Luồng chuyển đổi trạng thái phòng:** `TRONG` → `DA_DAT` → `DA_THUE`.

Ý nghĩa chi tiết từng trạng thái:
1. **`TRONG` (Phòng trống):** Phòng sạch sẽ, sẵn sàng cho khách mới đến xem, đặt giữ chỗ hoặc ký hợp đồng thuê ngay.
2. **`DA_DAT` (Đã đặt cọc / Giữ chỗ):** Phòng đã được khách đặt giữ chỗ (qua Web hoặc lệnh `BOOK` của Socket Client) và được lưu vết trong bảng `dat_cho`. Khách khác không thể đặt trùng. Nếu hết hạn giữ chỗ mà chưa ký hợp đồng, tác vụ `@Scheduled` sẽ tự động hủy và đưa phòng trở lại trạng thái `TRONG`.
3. **`DA_THUE` (Đang thuê):** Phòng đã có hợp đồng thuê phòng có hiệu lực pháp lý và khách đang cư trú thực tế.
4. **`BAO_TRI` (Bảo trì / Sửa chữa):** Phòng đang gặp sự cố hư hại thiết bị hoặc đang nâng cấp, tạm thời đóng không cho thuê hay đặt giữ chỗ.

---

## III. THIẾT KẾ CƠ SỞ DỮ LIỆU DỰ KIẾN (MICROSOFT SQL SERVER)

Hệ thống lưu trữ trên **Microsoft SQL Server**, cơ sở dữ liệu `NhaTroDB`. Các bảng được thiết kế chuẩn hóa, khóa chính tự tăng (`IDENTITY(1,1)`), toàn bộ liên kết thông qua Khóa ngoại (`FOREIGN KEY`).

### 1. Sơ đồ quan hệ thực thể (ERD)

```
                     ┌──────────────────┐
                     │    phong_tro     │
                     └────────┬─────────┘
                              │
          ┌───────────────────┼───────────────────┐
    1 - N │             1 - N │             1 - N │
          ▼                   ▼                   ▼
   ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐
   │   dat_cho    │    │   hop_dong   │    │ yeu_cau_sua_chua │
   └──────▲───────┘    └──────▲───┬───┘    └────────▲─────────┘
          │                   │   │                 │
    1 - N │             1 - N │   │ 1 - N           │ 1 - N
          │                   │   ▼                 │
          │                   │ ┌──────────────┐    │
          │                   │ │   hoa_don    │    │
          │                   │ └──────────────┘    │
          │                   │                     │
          └───────────┬───────┴─────────────────────┘
                      │
                ┌─────┴────────┐
                │  khach_thue  │
                └─────┬────────┘
                      │
                1 - N │ (FK khach_id Nullable cho Admin/Staff)
                      ▼
                ┌──────────────┐
                │  tai_khoan   │
                └──────────────┘
```

*Tóm tắt mối quan hệ:*
- `phong_tro` 1 – N `dat_cho`
- `khach_thue` 1 – N `dat_cho`
- `phong_tro` 1 – N `hop_dong`
- `khach_thue` 1 – N `hop_dong`
- `hop_dong` 1 – N `hoa_don`
- `phong_tro` 1 – N `yeu_cau_sua_chua`
- `khach_thue` 1 – N `yeu_cau_sua_chua`
- `khach_thue` 1 – N `tai_khoan` (`khach_id` là Foreign Key, cho phép NULL đối với tài khoản Quản trị viên và Nhân viên)

> **Ghi chú:** Sơ đồ ERD chính thức hoàn chỉnh với đầy đủ thuộc tính và kiểu dữ liệu chi tiết sẽ được vẽ bằng **draw.io** để nộp kèm trong cuốn Báo cáo đồ án.

---

### 2. Chi tiết cấu trúc 7 bảng và người phụ trách

#### Bảng 1: `phong_tro` (Quản lý phòng trọ)
- **Người phụ trách:** Nguyễn Minh Tâm
- **Cấu trúc:**
  - `id` (INT, IDENTITY(1,1), PK)
  - `ma_phong` (VARCHAR(20), UNIQUE, NOT NULL)
  - `ten_phong` (NVARCHAR(50), NOT NULL)
  - `loai_phong` (NVARCHAR(30)) — Đơn, Đôi, Gác lửng, Studio
  - `dien_tich` (FLOAT) — Đơn vị: m²
  - `gia_thue` (DECIMAL(12,2), NOT NULL)
  - `hinh_anh` (VARCHAR(255)) — Đường dẫn / URL ảnh phòng
  - `trang_thai` (VARCHAR(20), NOT NULL) — `TRONG`, `DA_DAT`, `DA_THUE`, `BAO_TRI`
  - `mo_ta` (NVARCHAR(255))
- **Dữ liệu mẫu:** Tối thiểu 20 bản ghi mẫu (từ P101 đến P405).

#### Bảng 2: `khach_thue` (Quản lý khách thuê)
- **Người phụ trách:** Nguyễn Minh Tâm
- **Cấu trúc:**
  - `id` (INT, IDENTITY(1,1), PK)
  - `ho_ten` (NVARCHAR(100), NOT NULL)
  - `cccd` (VARCHAR(20), UNIQUE, NOT NULL)
  - `so_dien_thoai` (VARCHAR(15), NOT NULL)
  - `email` (VARCHAR(100))
  - `ngay_sinh` (DATE)
  - `que_quan` (NVARCHAR(150))
  - `gioi_tinh` (NVARCHAR(10))
  - `bien_so_xe` (VARCHAR(20))
  - `trang_thai` (VARCHAR(20)) — `DANG_THUE`, `DA_TRA_PHONG`
- **Dữ liệu mẫu:** Tối thiểu 20 bản ghi mẫu khách thuê thực tế.

#### Bảng 3: `tai_khoan` (Hệ thống tài khoản & Xác thực)
- **Người phụ trách:** Nguyễn Minh Tâm
- **Cấu trúc:**
  - `id` (INT, IDENTITY(1,1), PK)
  - `ten_dang_nhap` (VARCHAR(50), UNIQUE, NOT NULL)
  - `mat_khau` (VARCHAR(100), NOT NULL) — Lưu chuỗi băm SHA-256 kèm salt tạo bằng `java.security.MessageDigest`
  - `vai_tro` (VARCHAR(20), NOT NULL) — `ADMIN`, `STAFF`, `TENANT`
  - `khach_id` (INT, FK -> `khach_thue.id`, NULLABLE) — Khách thuê thì liên kết tới hồ sơ khách, Admin/Staff thì để `NULL`.
  - `trang_thai` (BIT, DEFAULT 1) — 1: Hoạt động, 0: Bị khóa
  - `ngay_tao` (DATETIME DEFAULT GETDATE())
- **Dữ liệu mẫu:** Tối thiểu 20 bản ghi (1 tài khoản Admin, 2 tài khoản Nhân viên, 17+ tài khoản Khách thuê). Toàn bộ mật khẩu mẫu được lưu dưới dạng băm SHA-256 + salt đã tạo sẵn.

#### Bảng 4: `dat_cho` (Quản lý đặt giữ chỗ phòng trọ)
- **Người phụ trách:** [Thành viên 2]
- **Cấu trúc:**
  - `id` (INT, IDENTITY(1,1), PK)
  - `phong_id` (INT, FK -> `phong_tro.id`, NOT NULL)
  - `khach_id` (INT, FK -> `khach_thue.id`, NOT NULL)
  - `ngay_dat` (DATETIME, NOT NULL, DEFAULT GETDATE())
  - `han_giu_cho` (DATETIME, NOT NULL) — Thời hạn hết hiệu lực giữ chỗ
  - `trang_thai` (VARCHAR(30), NOT NULL) — `DANG_GIU`, `HET_HAN`, `DA_CHUYEN_HOP_DONG`
  - `ghi_chu` (NVARCHAR(255))
- **Dữ liệu mẫu:** Tối thiểu 10 bản ghi đặt chỗ mẫu (bao gồm các bản ghi đang giữ chỗ, hết hạn và đã chuyển thành hợp đồng).

#### Bảng 5: `hop_dong` (Quản lý hợp đồng thuê phòng)
- **Người phụ trách:** [Thành viên 2]
- **Cấu trúc:**
  - `id` (INT, IDENTITY(1,1), PK)
  - `ma_hop_dong` (VARCHAR(30), UNIQUE, NOT NULL)
  - `phong_id` (INT, FK -> `phong_tro.id`, NOT NULL)
  - `khach_id` (INT, FK -> `khach_thue.id`, NOT NULL)
  - `ngay_bat_dau` (DATE, NOT NULL)
  - `ngay_ket_thuc` (DATE, NOT NULL)
  - `tien_dat_coc` (DECIMAL(12,2), NOT NULL)
  - `gia_thue_thoa_thuan` (DECIMAL(12,2), NOT NULL)
  - `chu_ky_thanh_toan` (INT, DEFAULT 1) — Số tháng/kỳ
  - `trang_thai` (VARCHAR(20), NOT NULL) — `HIEU_LUC`, `HET_HAN`, `DA_THANH_LY`
- **Dữ liệu mẫu:** Tối thiểu 20 bản ghi hợp đồng gắn kết phòng và khách thuê.

#### Bảng 6: `hoa_don` (Quản lý hóa đơn thu tiền)
- **Người phụ trách:** [Thành viên 2]
- **Cấu trúc:**
  - `id` (INT, IDENTITY(1,1), PK)
  - `ma_hoa_don` (VARCHAR(30), UNIQUE, NOT NULL)
  - `hop_dong_id` (INT, FK -> `hop_dong.id`, NOT NULL)
  - `thang_nam` (VARCHAR(7), NOT NULL) — Ví dụ: `10/2026`
  - **Ràng buộc duy nhất:** `CONSTRAINT UQ_hoa_don_thang UNIQUE (hop_dong_id, thang_nam)`
  - `chi_so_dien_cu` (INT, NOT NULL)
  - `chi_so_dien_moi` (INT, NOT NULL)
  - `don_gia_dien` (DECIMAL(10,2), DEFAULT 3500)
  - `chi_so_nuoc_cu` (INT, NOT NULL)
  - `chi_so_nuoc_moi` (INT, NOT NULL)
  - `don_gia_nuoc` (DECIMAL(10,2), DEFAULT 20000)
  - `tien_phong` (DECIMAL(12,2), NOT NULL)
  - `tien_dien` (DECIMAL(12,2), NOT NULL)
  - `tien_nuoc` (DECIMAL(12,2), NOT NULL)
  - `tien_dich_vu` (DECIMAL(12,2), DEFAULT 100000)
  - `tong_tien` (DECIMAL(12,2), NOT NULL)
  - `trang_thai` (VARCHAR(20), NOT NULL) — `CHUA_THANH_TOAN`, `DA_THANH_TOAN`
  - `ngay_lap` (DATETIME DEFAULT GETDATE())
  - `ngay_thanh_toan` (DATETIME NULL)
- **Dữ liệu mẫu:** Tối thiểu 20 bản ghi hóa đơn hàng tháng.

#### Bảng 7: `yeu_cau_sua_chua` (Quản lý sự cố & sửa chữa cơ sở vật chất)
- **Người phụ trách:** [Thành viên 4]
- **Cấu trúc:**
  - `id` (INT, IDENTITY(1,1), PK)
  - `phong_id` (INT, FK -> `phong_tro.id`, NOT NULL)
  - `khach_id` (INT, FK -> `khach_thue.id`, NOT NULL)
  - `noi_dung` (NVARCHAR(255), NOT NULL)
  - `tho_sua_chua` (NVARCHAR(100)) — Họ tên thợ phụ trách
  - `chi_phi` (DECIMAL(12,2), DEFAULT 0) — Chi phí sửa chữa
  - `muc_do` (VARCHAR(20), NOT NULL) — `BINH_THUONG`, `CAP_BACH`
  - `trang_thai` (VARCHAR(20), NOT NULL) — `CHO_TIEP_NHAN`, `DANG_SUA`, `HOAN_THANH`
  - `ngay_tao` (DATETIME DEFAULT GETDATE())
  - `ngay_hoan_thanh` (DATETIME NULL)
- **Dữ liệu mẫu:** Tối thiểu 20 bản ghi phản ánh sự cố.

---

### 3. Quy chuẩn tính nhất quán của dữ liệu mẫu (Sample Data Integrity)
Dữ liệu mẫu trong script `sql/02_schema_and_sample_data.sql` bắt buộc phải bảo đảm tính nhất quán logic giữa các bảng:
1. **Phòng có hợp đồng `HIEU_LUC`** thì trạng thái phòng trong `phong_tro` bắt buộc phải là **`DA_THUE`**.
2. **Mỗi phòng chỉ có tối đa 1 hợp đồng ở trạng thái `HIEU_LUC`** tại một thời điểm.
3. **Phải có một số hợp đồng ở trạng thái `HET_HAN` hoặc `DA_THANH_LY`** để lưu vết lịch sử cư trú và phục vụ kiểm thử tính năng lọc/thống kê lịch sử thuê.
4. **Phòng có trạng thái `DA_DAT`** thì phải có bản ghi tương ứng trong `dat_cho` với trạng thái `DANG_GIU` và `han_giu_cho` còn hiệu lực.
5. **Mật khẩu trong `tai_khoan`** phải được băm sẵn bằng SHA-256 + salt tương thích chính xác với thuật toán giải mã của Service xác thực.

---

## IV. ĐỀ XUẤT VÀ HIỆN THỰC XỬ LÝ ĐỒNG THỜI VÀ ĐA LUỒNG

Trong đề tài này, nhóm hiện thực **hai cơ chế đa luồng & đồng thời** trọng tâm:
1. **Cơ chế 1: Khóa bi quan (`findByIdForUpdate`) chống thuê / đặt trùng phòng (Concurrency Lock)**
2. **Cơ chế 2: Tác vụ nền định kỳ (`@Scheduled`) tự động giải phóng phòng giữ chỗ quá hạn (Background Scheduled Task)**

---

### 1. Cơ chế 1: Chống đặt/thuê trùng phòng bằng Pessimistic Lock
- **Người phụ trách chính:** **[Thành viên 2]**
- **Nghiệp vụ:** Khi có hai hoặc nhiều yêu cầu đặt giữ chỗ / tạo hợp đồng cho cùng một phòng trọ (ví dụ phòng `P201`) gửi đến đồng thời từ Web hoặc Socket Server, hệ thống chỉ cho phép một yêu cầu thành công, các yêu cầu còn lại bị từ chối và nhận thông báo lỗi thích hợp.
- **Giải pháp kỹ thuật:** Cả phương thức xử lý tạo hợp đồng trên Web và lệnh `BOOK` của Socket Server đều gọi chung qua tầng Service, sử dụng phương thức truy vấn khóa bi quan (**Pessimistic Write Lock**) trong `PhongTroRepository`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM PhongTro p WHERE p.id = :id")
Optional<PhongTro> findByIdForUpdate(@Param("id") Integer id);
```

- **Nguyên lý hoạt động trên Microsoft SQL Server:**
  - Khi luồng đầu tiên bắt đầu Transaction (`@Transactional`) và gọi `findByIdForUpdate`, Hibernate sinh câu truy vấn `SELECT ... WITH (UPDLOCK, ROWLOCK)` khóa bản ghi phòng trọ đó lại.
  - Các luồng tiếp theo cùng truy vấn bản ghi này sẽ bị tạm dừng và đưa vào hàng đợi chờ giải phóng khóa.
  - Luồng 1 kiểm tra điều kiện:
    - Nếu là đặt chỗ: kiểm tra trạng thái phòng có phải `TRONG` không. Nếu hợp lệ, ghi bản ghi vào `dat_cho`, cập nhật trạng thái phòng thành `DA_DAT`, commit transaction và nhả khóa.
    - Nếu là tạo hợp đồng: kiểm tra phòng có đang `TRONG` hoặc đang `DA_DAT` đúng khách đã giữ chỗ không. Nếu hợp lệ, tạo hợp đồng, cập nhật `dat_cho` thành `DA_CHUYEN_HOP_DONG`, chuyển phòng sang `DA_THUE`, commit transaction và nhả khóa.
  - Luồng 2 sau đó được tiếp tục thực thi, đọc thấy trạng thái phòng không còn thỏa mãn, lập tức ném ngoại lệ nghiệp vụ (`RoomAlreadyBookedException`).
  - Kết quả: Hệ thống **bảo đảm tính toàn vẹn dữ liệu (Data Integrity)**, loại bỏ hoàn toàn nguy cơ trùng lặp đặt phòng.

---

### 2. Bộ kiểm thử đồng thời độc lập (`ConcurrencyBookingTest.java`) và Script Reset
- **Người phụ trách:** **[Thành viên 4]**
- **Yêu cầu kỹ thuật:** Viết thành một ứng dụng Client độc lập chạy bằng hàm `main()`, thuần chuẩn Java Core (`java.util.concurrent`, `Socket`), **hoàn toàn không sử dụng thêm bất kỳ thư viện ngoài nào khác**.
- **Cơ chế kiểm thử:**
  - Khởi tạo Thread Pool gồm 10 luồng: `ExecutorService pool = Executors.newFixedThreadPool(10);`
  - Sử dụng hai biến đếm `CountDownLatch readyLatch = new CountDownLatch(10);` và `CountDownLatch startLatch = new CountDownLatch(1);` để ép 10 luồng kết nối Socket sẵn sàng và đồng loạt kích hoạt gửi lệnh `BOOK P201#...` tới cổng 9999 tại cùng một thời điểm.
  - Ghi nhận kết quả: Chính xác **1 luồng nhận phản hồi OK** (đặt phòng thành công) và **9 luồng nhận phản hồi từ chối / lỗi** do phòng đã bị khóa/đã được đặt.
- **Script chuẩn bị dữ liệu kiểm thử (`sql/reset_p201.sql`):**
  - Để đảm bảo bài kiểm thử có thể chạy lặp lại nhiều lần với kết quả nhất quán, nhóm xây dựng script `sql/reset_p201.sql` nhằm xóa các bản ghi `dat_cho` liên quan đến `P201` và đưa phòng `P201` về trạng thái `TRONG` trước mỗi lần chạy test:
    ```sql
    DELETE FROM dat_cho WHERE phong_id IN (SELECT id FROM phong_tro WHERE ma_phong = 'P201');
    UPDATE phong_tro SET trang_thai = 'TRONG' WHERE ma_phong = 'P201';
    ```
- Toàn bộ nhật ký (log) kiểm thử và bảng số liệu đối soát sẽ được đưa vào **Chương 4 của Báo cáo đồ án**.

---

### 3. Cơ chế 2: Tác vụ nền tự động giải phóng đặt chỗ quá hạn (@Scheduled)
- **Người phụ trách chính:** **[Thành viên 2]**
- **Nghiệp vụ:** Khách hàng giữ chỗ phòng chỉ được giữ tối đa trong một khoảng thời gian (ví dụ: 24 giờ kể từ thời điểm đặt). Nếu quá thời hạn này mà khách không đến làm hợp đồng thuê chính thức, phòng phải được tự động thu hồi về trạng thái `TRONG` để khách khác có thể đặt.
- **Giải pháp kỹ thuật:**
  - Bật tính năng lập lịch chạy nền của Spring bằng `@EnableScheduling`.
  - Định nghĩa tác vụ nền chạy định kỳ (ví dụ mỗi 60 giây):
    ```java
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void autoReleaseExpiredBookings() {
        // Quét các bản ghi dat_cho có trang_thai = 'DANG_GIU' và han_giu_cho < LocalDateTime.now()
        // Cập nhật dat_cho sang 'HET_HAN'
        // Cập nhật phong_tro tương ứng từ 'DA_DAT' về 'TRONG'
    }
    ```
  - Đây là minh chứng rõ nét cho việc ứng dụng kỹ thuật Đa luồng xử lý nền (Background Thread Task) trong đồ án.

---

## V. ĐỀ XUẤT VÀ HIỆN THỰC MẠNG SOCKET (MULTI-CLIENT SOCKET)

- **Người phụ trách chính:** **[Thành viên 4]**
- **Kiến trúc Server:**
  - `SocketServer` lắng nghe trên cổng riêng biệt `Port 9999` (độc lập với cổng Web `8080`).
  - Khởi động tự động trong vòng đời ứng dụng Spring Boot thông qua việc hiện thực interface `CommandLineRunner` (chạy trên một background thread riêng để không block luồng khởi động Spring).
  - Quản lý các kết nối đến bằng `ExecutorService` (Thread Pool), phục vụ tối thiểu **3 Client kết nối và tương tác cùng lúc**.
  - **Tiêm phụ thuộc (Dependency Injection):** `ClientHandler` được nhận các Spring Service (`DatChoService`, `PhongTroService`) qua cơ chế tiêm phụ thuộc của Spring (không dùng từ khóa `new` Service trong Handler). Điều này giúp bảo toàn ngữ cảnh Spring Container, bảo đảm cơ chế quản lý giao dịch `@Transactional` và khóa bi quan `findByIdForUpdate` hoạt động chuẩn xác trong từng luồng Socket.
  - **Quy chuẩn mã nguồn bắt buộc:** Sử dụng triệt để cú pháp **`try-with-resources`** cho `ServerSocket`, `Socket`, `BufferedReader`, `PrintWriter` để tự động giải phóng socket và stream khi client ngắt kết nối, bắt và ghi nhận lỗi `IOException` / `SocketException` mà không gây dừng máy chủ:
    ```java
    try (Socket clientSocket = serverSocket.accept();
         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
         PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {
        // Xử lý gói tin trao đổi với Client
    } catch (IOException e) {
        log.error("Lỗi kết nối Socket Client: " + e.getMessage());
    }
    ```
- **Ứng dụng Desktop Client độc lập:** Nằm tại `client/src/com/nhatro/client/SocketClientApp.java`, chạy hàm `main()` độc lập, cung cấp giao diện tương tác (Console hoặc Swing đơn giản) để tra cứu phòng và gửi lệnh đặt chỗ.
- **Giao thức trao đổi dữ liệu (Socket Protocol):**
  - Lệnh kiểm tra danh sách phòng: `LIST_ROOM`
  - Lệnh đặt giữ chỗ phòng: `BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt>`
  - Khi nhận lệnh `BOOK`, Socket Server gọi trực tiếp tầng Service có `findByIdForUpdate` để khóa phòng, ghi nhận vào bảng `dat_cho`, cập nhật trạng thái phòng sang `DA_DAT` và phản hồi kết quả về Client: `OK#Dat cho thanh cong` hoặc `FAIL#Phong da duoc dat hoac dang thue`.

---

## VI. LỊCH LÀM VIỆC THEO TUẦN (PHÂN VIỆC CHO CẢ 4 NGƯỜI TỪ TUẦN 1)

*Lịch làm việc được tổ chức theo từng tuần học kỳ, lùi từ mốc nộp đồ án. Chừa ít nhất 2 ngày dự phòng trước hạn nộp chính thức.*

| Tuần | Công việc của 4 thành viên | Báo cáo & Tài liệu thiết kế | Kết quả bàn giao (Deliverables) |
|:---:|:---|:---|:---|
| **Tuần 1** | - **TV1 (Trưởng nhóm):** Trong 2–3 ngày đầu, hoàn thành Sơ đồ thực thể quan hệ (**ERD** trên draw.io), viết script SQL (`01_create_database.sql`, `02_schema_and_sample_data.sql`, `reset_p201.sql`), viết toàn bộ các lớp JPA Entity thủ công (không dùng Lombok) và đẩy lên nhánh `dev` để unblock các thành viên khác.<br>- **TV2:** Khảo sát quy trình nghiệp vụ Hợp đồng - Đặt chỗ - Hóa đơn, vẽ biểu đồ **Use Case** tổng thể.<br>- **TV3:** Thiết kế Wireframe giao diện, vẽ **Sơ đồ kiến trúc phân lớp** Controller - Service - Repository - View.<br>- **TV4:** Thiết kế đặc tả giao thức mạng Socket (Protocol Specification), xây dựng khung `SocketServer` đa luồng bằng try-with-resources. | **Soạn thảo Chương 1 của Báo cáo đồ án:**<br>- Lý do chọn đề tài, khảo sát thực tế.<br>- Mục tiêu và phạm vi nghiệp vụ.<br>- Phân việc cho cả 4 thành viên từ tuần đầu. | - Script SQL chuẩn chạy trên SSMS.<br>- JPA Entities sẵn sàng trên nhánh `dev`.<br>- Bản nháp Chương 1 Báo cáo.<br>- Sơ đồ ERD (draw.io), Use Case, Kiến trúc phân lớp. |
| **Tuần 2** | - **TV1:** Viết tầng JPA Repository, hiện thực trọn gói (Controller, Service, View) cho CRUD danh mục Phòng trọ (kèm ảnh) và Khách thuê.<br>- **TV2:** Xây dựng CRUD Hợp đồng thuê phòng, vẽ **Sơ đồ tuần tự (Sequence Diagram) luồng "Tạo hợp đồng"**.<br>- **TV3:** Thiết lập Layout Thymeleaf + Bootstrap 5.3 CDN, hoàn thiện giao diện danh sách phòng (tìm kiếm, phân trang, sort, modal xác nhận xóa, lỗi cạnh ô nhập, flash message).<br>- **TV4:** Thiết kế chi tiết luồng xử lý `ClientHandler` đa luồng cho Socket Server. | **Soạn thảo Chương 2 của Báo cáo đồ án:**<br>- Phân tích yêu cầu chức năng, phi chức năng.<br>- Thiết kế CSDL chi tiết.<br>- Biểu đồ tuần tự luồng nghiệp vụ. | - CRUD Phòng trọ & Khách thuê chạy mượt.<br>- Giao diện web có phân trang, tìm kiếm.<br>- Bản nháp Chương 2 Báo cáo. |
| **Tuần 3** | - **TV1:** Xây dựng hệ thống Tài khoản (`tai_khoan`), mã hóa mật khẩu SHA-256 kèm salt bằng `MessageDigest`, form đăng nhập và bộ chặn phân quyền `HandlerInterceptor`.<br>- **TV2:** **Hiện thực cơ chế XỬ LÝ ĐỒNG THỜI** (`findByIdForUpdate` - Pessimistic Lock) và nghiệp vụ Đặt chỗ (`dat_cho`) chống đặt trùng phòng trên Web.<br>- **TV3:** Xây dựng giao diện Khách thuê tra cứu phòng trực tuyến, xem chi tiết phòng, form gửi yêu cầu sửa chữa.<br>- **TV4:** Xây dựng trọn gói Backend và Giao diện cho Module Quản lý sự cố / Sửa chữa cơ sở vật chất (phân công thợ, cập nhật chi phí). | **Cập nhật Báo cáo:**<br>- Bổ sung tài liệu phân tích cơ chế Concurrency Locking.<br>- Thiết kế các luồng bảo mật qua Session. | - Chức năng Đăng nhập & Phân quyền hoàn tất.<br>- Chống thuê/đặt trùng phòng hoạt động chuẩn xác.<br>- Chặn xóa dữ liệu có ràng buộc toàn vẹn. |
| **Tuần 4** | - **TV1:** Hoàn thiện bảng dữ liệu mẫu 20 bản ghi mỗi bảng trong SQL Server, bảo đảm tính nhất quán (phòng có hợp đồng hiệu lực thì `DA_THUE`, hợp đồng hết hạn/thanh lý, mật khẩu đã băm).<br>- **TV2:** Hoàn thiện ghi chỉ số Điện - Nước, tự động tính toán hóa đơn hàng tháng (`UNIQUE(hop_dong_id, thang_nam)`), và cài đặt **tác vụ nền `@Scheduled` quét giải phóng phòng đặt chỗ quá hạn**.<br>- **TV3:** Thiết kế giao diện in hóa đơn, màn hình thu ngân gạch nợ, tối ưu giao diện Cổng thông tin khách thuê.<br>- **TV4:** **Hoàn thiện Mạng Socket đa Client:** Khởi động qua `CommandLineRunner`, `ClientHandler` nhận Service qua Dependency Injection, tích hợp lệnh `BOOK` gọi `findByIdForUpdate`. | **Soạn thảo Chương 3 của Báo cáo đồ án:**<br>- Hiện thực các chức năng chính.<br>- Chi tiết cài đặt Socket, Tác vụ nền `@Scheduled` và Cơ chế đồng thời. | - Hệ thống Socket phục vụ >= 3 Client cùng lúc.<br>- Hóa đơn điện nước tính toán chính xác.<br>- Tác vụ `@Scheduled` chạy nền ổn định.<br>- Bản nháp Chương 3 Báo cáo. |
| **Tuần 5** | - **TV1:** Rà soát toàn bộ ràng buộc khóa ngoại, chặn xóa phòng/khách đang có hợp đồng, chặn xóa hợp đồng đã có hóa đơn, báo lỗi thân thiện.<br>- **TV2:** Tinh chỉnh xử lý ngoại lệ nghiệp vụ và thông báo lỗi thân thiện trên Web.<br>- **TV3:** Hoàn thiện giao diện thanh toán, rà soát CSS/Bootstrap CDN và độ phản hồi Responsive trên tất cả các trang của khách thuê.<br>- **TV4:** **Đảm nhận trọn gói Dashboard thống kê** (viết Service truy vấn, Controller và trang Thymeleaf biểu đồ) và **viết chương trình kiểm thử đồng thời độc lập** (`ConcurrencyBookingTest.java` dùng `CountDownLatch` và Socket). | **Tiến hành đo đạc & Viết Chương 4:**<br>- Chạy kiểm thử đa luồng với `sql/reset_p201.sql`, chụp log kết quả.<br>- Đánh giá hiệu năng và tính toàn vẹn dữ liệu. | - Chương trình kiểm thử Concurrency độc lập chạy đạt kết quả (1 OK, 9 REJECT).<br>- Dashboard thống kê hoàn chỉnh.<br>- Báo cáo Chương 4 hoàn thành. |
| **Tuần 6** | - **Cả 4 thành viên cùng phối hợp:**<br>+ Kiểm thử tích hợp toàn diện hệ thống (Integration Test).<br>+ Chạy đồng thời 3 Desktop Socket Client + nhiều phiên duyệt web.<br>+ Rà soát kiểm tra mã nguồn, chuẩn hóa chú thích (comment), xóa mã rác.<br>+ Định dạng tài liệu báo cáo đồ án hoàn chỉnh (**25 đến 40 trang**). | **Nghiệm thu toàn bộ tài liệu:**<br>- Hoàn chỉnh cuốn Báo cáo Đồ án.<br>- Thiết kế Slide thuyết trình (**tối đa 20 trang**). | - Cuốn Báo cáo Word/PDF hoàn chỉnh.<br>- Bản Slide thuyết trình chuyên nghiệp. |
| **Tuần 7** | - **Cả 4 thành viên cùng phối hợp:**<br>+ Tập dượt thuyết trình nhóm (khống chế đúng thời gian **15 phút**).<br>+ Phân vai từng thành viên thuyết trình và demo phần mã nguồn mình phụ trách.<br>+ Tập trả lời các câu hỏi phản biện, vấn đáp lý thuyết & mã nguồn của giảng viên. | **Nghiệm thu nội bộ sản phẩm:**<br>- Đóng gói mã nguồn sạch.<br>- Video demo dự phòng. | - Cả nhóm nắm vững và giải thích cặn kẽ toàn bộ mã nguồn khi vấn đáp. |
| **Tuần 8** | - Rà soát lần cuối toàn bộ tài liệu và mã nguồn.<br>- **Chừa ít nhất 2 ngày dự phòng** trước hạn nộp chính thức.<br>- Nộp báo cáo và mã nguồn lên hệ thống của trường đúng quy định. | **Hoàn tất thủ tục nộp bài:**<br>- Báo cáo, Slide, Mã nguồn Git. | - Đồ án nộp thành công đúng hạn. |

---

## VII. QUY TẮC LÀM VIỆC TRÊN GIT CỦA NHÓM

Để minh bạch tiến độ và bảo đảm điểm số cá nhân khi giảng viên kiểm tra nhật ký Git:
1. **Quy tắc phân nhánh:**
   - Nhánh `main`: Nhánh ổn định cao nhất, tuyệt đối **không ai được push trực tiếp vào `main`**.
   - Nhánh `dev`: Nhánh phát triển chung để tích hợp mã nguồn của cả nhóm.
   - Nhánh tính năng cá nhân:
     - Trưởng nhóm Tâm: `feature/db-entity-tam`
     - Thành viên 2: `feature/contract-billing`
     - Thành viên 3: `feature/ui-thymeleaf`
     - Thành viên 4: `feature/socket-network`
2. **Cam kết đóng góp (Commit Rules):**
   - Mỗi thành viên bắt buộc sử dụng **tài khoản GitHub cá nhân** của mình để commit đều đặn từ tuần 1 đến tuần 8.
   - Nội dung commit phải rõ ràng theo chuẩn Conventional Commits (Ví dụ: `feat: tao bang dat cho va scheduled task`, `test: viet chuong trinh kiem thu concurrency`).
3. **Quy trình gộp mã nguồn (Pull Request & Code Review):**
   - Trước khi code: Kéo code mới nhất từ `dev` về (`git pull origin dev`).
   - Sau khi hoàn thành và tự test trên máy: Đẩy nhánh tính năng lên GitHub và mở **Pull Request (PR)** gộp vào `dev`.
   - Phải có ít nhất Trưởng nhóm hoặc một thành viên khác xem xét (Code Review) và phê duyệt trước khi merge vào `dev`.

---

## VIII. CÁC ĐIỂM CẦN HỎI VÀ XIN Ý KIẾN GIẢNG VIÊN

1. **Về hình thức nộp và lịch bảo vệ:** Hạn nộp báo cáo chính thức và ngày thuyết trình/vấn đáp đồ án rơi vào tuần thứ mấy của học kỳ?
2. **Về kho lưu trữ GitHub:** Kho lưu trữ mã nguồn của nhóm cần để chế độ Public hay để Private rồi mời tài khoản GitHub của giảng viên vào (Collaborator)?
3. **Về phạm vi đa luồng:** Phần giữ chỗ tự hết hạn bằng tác vụ nền `@Scheduled` có được tính là phần đa luồng thứ hai của đồ án không?
4. **Về triển khai Socket Server:** Giảng viên yêu cầu Socket Server khởi động cùng ứng dụng Spring Boot thông qua `CommandLineRunner` (lắng nghe cổng phụ 9999) hay chạy dưới dạng một ứng dụng console Java độc lập hoàn toàn?
5. **Về phương thức kiểm thử Concurrency:** Giảng viên mong muốn nhóm chứng minh phần Concurrency bằng chương trình chạy hàm `main()` độc lập in log ra màn hình (`ConcurrencyBookingTest.java`), hay có thể demo trực tiếp trên 2 trình duyệt bấm nút cùng lúc?
6. **Về định dạng báo cáo:** Cuốn báo cáo nộp file mềm PDF qua hệ thống LMS hay yêu cầu in đóng cuốn bìa kiếng, và slide thuyết trình có mẫu template bắt buộc riêng của khoa hay không?
