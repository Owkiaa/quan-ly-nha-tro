# KẾ HOẠCH VÀ BẢNG PHÂN CÔNG ĐỒ ÁN MÔN CÔNG NGHỆ JAVA

**Đề tài:** Hệ Thống Quản Lí Nhà Trọ  
**Công nghệ:** Spring Boot 3.3.5, Spring Data JPA, Thymeleaf, Microsoft SQL Server, Bootstrap 5.3 (CDN), Java Socket  
**Nhánh Git làm việc chính:** `dev`  
**Hạn nộp dự kiến:** **[Chờ xác nhận từ giảng viên]**  

---

## I. THÔNG TIN NHÓM VÀ BẢNG PHÂN CÔNG NHIỆM VỤ CHÍNH

Nhóm gồm **5 thành viên**. Trách nhiệm được phân bổ đồng đều giữa 5 thành viên theo các mảng kiến trúc và nghiệp vụ chuyên sâu. Khi vấn đáp, từng thành viên **phải tự trình bày và giải thích cặn kẽ toàn bộ phần mã nguồn mình đã thực hiện**.

| STT | Họ và Tên | MSSV | Mảng phụ trách chính | Phạm vi trách nhiệm & Vấn đáp |
|:---:|:---|:---:|:---|:---|
| **1** | **Nguyễn Minh Tâm** | 2001240423 | **Cơ sở dữ liệu, Kiến trúc tổng thể, Phòng trọ, Khách thuê, Tài khoản & Xác thực** | - Thiết kế ERD, Schema CSDL SQL Server (`NhaTroDB`), viết script SQL tạo database và dữ liệu mẫu.<br>- Hoàn thành và đẩy các Entity & Repository thủ công lên nhánh `dev` trong 2–3 ngày đầu của Tuần 1 để unblock nhóm.<br>- Tái sử dụng bộ Thymeleaf Fragment của TV3 để hiện thực trọn gói (Controller, Service, View) cho CRUD Phòng trọ (kèm ảnh), Khách thuê.<br>- Quản lý tài khoản, Đăng nhập và Phân quyền bằng `HttpSession` & `HandlerInterceptor` (không dùng Spring Security).<br>- Mã hóa mật khẩu bằng SHA-256 kèm salt (`java.security.MessageDigest`).<br>- Ràng buộc chặn xóa phòng và khách thuê đang có hợp đồng hoặc đang giữ chỗ (`dat_cho` còn `DANG_GIU`).<br>- Quản lý cấu trúc dự án và điều phối Git. |
| **2** | **Bùi Trần Tấn Tài** | 2001240413 | **Nghiệp vụ Hợp đồng, Đặt chỗ, Xử lý ĐỒNG THỜI & Tác vụ nền @Scheduled** | - Tái sử dụng bộ Thymeleaf Fragment của TV3 để hiện thực trọn gói (Controller, Service, View) cho Hợp đồng thuê phòng, Đặt chỗ (`dat_cho`).<br>- Ràng buộc toàn vẹn khi lập/thanh lý hợp đồng.<br>- **Thiết kế và hiện thực cơ chế XỬ LÝ ĐỒNG THỜI (Concurrency Lock)** bằng `findByIdForUpdate` (Pessimistic Write Lock) chống thuê/đặt trùng phòng. Lệnh đặt phòng khóa phòng trước, tìm/tạo khách sau trong cùng 1 Transaction.<br>- **Hiện thực tác vụ nền `@Scheduled`** định kỳ quét đặt chỗ quá hạn (tách `DatChoService` và `DatChoScheduler` để Spring AOP proxy quản lý `@Transactional` và lock chuẩn xác) để tự động hủy và trả phòng về `TRONG`; ghi log `Thread.currentThread().getName()`.<br>- Giải thích cơ chế Transaction, Isolation Level, Lock và Background Task khi vấn đáp. |
| **3** | **Lưu Quốc Trụ** | 2001240521 | **Kiến trúc Giao diện, Bộ Fragment dùng lại (Reusable Components) & Cổng Khách thuê (Tenant Portal)** | - Xây dựng khung giao diện chuẩn (Sidebar, Navbar, Footer) bằng Thymeleaf Layout Fragments.<br>- Tích hợp Bootstrap 5.3 CDN và CSS Responsive (Desktop/Mobile), màu sắc hài hòa, hiện đại.<br>- **Xây dựng bộ Thymeleaf Fragment dùng lại cho toàn nhóm**: thanh tìm kiếm, phân trang, sắp xếp cột dữ liệu, modal xác nhận xóa, flash alert (`RedirectAttributes`), và thông báo lỗi cạnh ô nhập liệu.<br>- Bàn giao bộ fragment cho TV1, TV2 và TV5 tích hợp vào các trang chức năng.<br>- **Hiện thực trọn gói Cổng Khách thuê (Tenant Portal)**: viết Controller, Service và View cho các tính năng: tra cứu phòng trống, xem chi tiết phòng, xem hợp đồng của mình, tra cứu hóa đơn các tháng, gửi phiếu phản ánh sự cố thiết bị. |
| **4** | **Đoàn Văn Mẩn** | 2001240270 | **Mạng Socket (Đa Client), Quản lý Sự cố & Bộ Kiểm thử Độc lập** | - Hiện thực Socket Server đa luồng (`ServerSocket`, `ExecutorService`) phục vụ tối thiểu **3 Client kết nối cùng lúc**.<br>- Cấu hình Socket Server khởi động cùng ứng dụng qua `CommandLineRunner`; tiêm Service vào `ClientHandler` qua Dependency Injection (không dùng `new`) để đảm bảo `@Transactional` và `findByIdForUpdate` hoạt động.<br>- Hỗ trợ đầy đủ các lệnh giao thức Socket: `LIST_ROOM`, `BOOK`, `EXIT`; ghi log tên luồng `Thread.currentThread().getName()` trong `ClientHandler`.<br>- Xây dựng ứng dụng Desktop Client độc lập (`SocketClientApp.java`) dùng try-with-resources.<br>- Hiện thực trọn gói module Quản lý sự cố / Yêu cầu sửa chữa (phân công thợ, cập nhật chi phí).<br>- Viết chương trình kiểm thử đồng thời độc lập `ConcurrencyBookingTest.java` (mở 10 luồng dùng 10 CCCD khác nhau gửi `BOOK P201` qua Socket, dùng `CountDownLatch`, đúng 1 luồng OK và 9 luồng bị từ chối). |
| **5** | **Trương Đỗ Minh Quân** | 2001240390 | **Chỉ số Điện Nước, Hóa đơn hàng tháng, Thu ngân, In hóa đơn & Dashboard Thống kê** | - Tái sử dụng bộ Thymeleaf Fragment của TV3 để hiện thực trọn gói (Controller, Service, View) cho Chỉ số Điện - Nước đầu kỳ/cuối kỳ và Lập hóa đơn tiền phòng hàng tháng.<br>- Ràng buộc duy nhất hóa đơn: `UNIQUE(hop_dong_id, thang_nam)`.<br>- Hiện thực trọn gói màn hình thu ngân, xác nhận thanh toán (gạch nợ) và in hóa đơn tiền phòng.<br>- Hiện thực trọn gói (Backend Service truy vấn số liệu, Controller, View Thymeleaf, Biểu đồ) cho Dashboard thống kê doanh thu theo tháng và công suất phòng.<br>- Giải thích quy trình chốt số điện nước, lập hóa đơn, thuật toán gạch nợ và truy vấn số liệu thống kê khi vấn đáp. |

---

## II. DANH SÁCH CHỨC NĂNG HỆ THỐNG VÀ QUY TẮC PHÂN CHIA (CHIA ĐỀU CHO 5 THÀNH VIÊN)

### 1. Quy tắc phân công phát triển mã nguồn
- **Mỗi thành viên tự viết trọn gói Controller, Service và template Thymeleaf cho chức năng mình phụ trách** (mô hình MVC hoàn chỉnh theo từng module nghiệp vụ).
- **Thành viên 3** chịu trách nhiệm thiết lập nền tảng giao diện: Layout chính, CSS toàn cục, Bootstrap 5.3 CDN và **xây dựng bộ Thymeleaf Fragment dùng lại (Reusable Components)** gồm: thanh tìm kiếm, phân trang, sắp xếp, modal xác nhận trước khi xóa, flash alert và thông báo lỗi cạnh ô nhập.
- **Thành viên 1, Thành viên 2 và Thành viên 5** trực tiếp sử dụng bộ Thymeleaf Fragment của Thành viên 3 cho các màn hình quản trị của mình, đảm bảo tính đồng bộ UI/UX toàn hệ thống.
- **Thành viên 2** tập trung vào phân hệ Hợp đồng, Giữ chỗ phòng, cơ chế khóa đồng thời `findByIdForUpdate` và tác vụ nền `@Scheduled`.
- **Thành viên 3** phụ trách trọn gói từ Controller, Service đến Giao diện cho toàn bộ Cổng Khách thuê (Tenant Portal).
- **Thành viên 4** phụ trách Mạng Socket đa client, Desktop Client, kiểm thử Concurrency độc lập và quản lý sự cố sửa chữa.
- **Thành viên 5** phụ trách toàn diện phân hệ Điện Nước, Hóa đơn hàng tháng, Thu ngân, In hóa đơn và Dashboard thống kê doanh thu - công suất phòng (trọn gói backend và view biểu đồ).

---

### 2. Danh sách 20 chức năng chi tiết (Mỗi chức năng 1 người chịu trách nhiệm chính)

#### Phân hệ 1: Quản trị danh mục & Xác thực (Thành viên 1 phụ trách chính)
1. **Quản lý danh mục phòng trọ (CRUD):** Thêm phòng mới, cập nhật diện tích, đơn giá, loại phòng, ảnh đại diện (`hinh_anh`), mô tả; cập nhật trạng thái phòng; tích hợp bộ fragment tìm kiếm, phân trang, modal xóa của TV3.  
   *👉 Người phụ trách chính: Nguyễn Minh Tâm*
2. **Quản lý hồ sơ khách thuê (CRUD):** Tiếp nhận thông tin định danh khách thuê (Họ tên, CCCD, SĐT, Email, Quê quán, Ngày sinh, Biển số xe); tích hợp bộ fragment của TV3.  
   *👉 Người phụ trách chính: Nguyễn Minh Tâm*
3. **Quản lý tài khoản & Phân quyền truy cập:** Đăng ký, đổi mật khẩu, đăng nhập hệ thống, kiểm tra quyền truy cập theo vai trò (`ADMIN`, `STAFF`, `TENANT`) bằng `HttpSession` và `HandlerInterceptor`.  
   *👉 Người phụ trách chính: Nguyễn Minh Tâm*
4. **Mã hóa và xác thực mật khẩu:** Sử dụng thuật toán SHA-256 kèm chuỗi muối (salt) thông qua thư viện chuẩn `java.security.MessageDigest`, không dùng Spring Security hay BCrypt. Dữ liệu mẫu lưu mật khẩu đã băm.  
   *👉 Người phụ trách chính: Nguyễn Minh Tâm*

#### Phân hệ 2: Hợp đồng, Đặt chỗ & Xử lý Đồng thời (Bùi Trần Tấn Tài phụ trách chính)
5. **Quản lý đặt giữ chỗ phòng (`dat_cho`):** Khách đặt giữ chỗ phòng qua Web hoặc Socket; ghi nhận thời hạn giữ chỗ (`han_giu_cho`). Khi đặt giữ chỗ thành công, chuyển phòng sang trạng thái `DA_DAT`.  
   *👉 Người phụ trách chính: Bùi Trần Tấn Tài*
6. **Quản lý hợp đồng thuê phòng:** Tạo hợp đồng thuê, quy định tiền cọc, ngày bắt đầu - kết thúc, chu kỳ đóng tiền, gia hạn hoặc thanh lý hợp đồng. Chỉ cho phép tạo hợp đồng với phòng đang `TRONG`, hoặc phòng đang `DA_DAT` nếu người ký hợp đồng đúng là khách đã giữ chỗ trong `dat_cho` (trạng thái `DANG_GIU`). Khi tạo xong, chuyển đặt chỗ sang `DA_CHUYEN_HOP_DONG` và phòng sang `DA_THUE`.  
   *👉 Người phụ trách chính: Bùi Trần Tấn Tài*
7. **Cơ chế khóa đồng thời chống thuê/đặt trùng phòng:** Hiện thực hàm `findByIdForUpdate` trong Repository kết hợp `@Transactional` để khóa dòng dữ liệu phòng trọ, dùng chung cho cả luồng Web và luồng Socket. Lệnh đặt phòng thực hiện khóa phòng trước, tìm/tạo khách sau trong cùng 1 transaction.  
   *👉 Người phụ trách chính: Bùi Trần Tấn Tài*
8. **Tác vụ nền tự động giải phóng chỗ quá hạn (@Scheduled):** Tác vụ nền quét định kỳ bảng `dat_cho`; tách `DatChoScheduler` gọi qua `DatChoService` để đảm bảo Spring Proxy kích hoạt `@Transactional` và `findByIdForUpdate`; kiểm tra lại `dat_cho` còn `DANG_GIU` trong Transaction rồi mới hủy và trả phòng về `TRONG`; ghi log rõ `Thread.currentThread().getName()`.  
   *👉 Người phụ trách chính: Bùi Trần Tấn Tài*

#### Phân hệ 3: Giao diện nền tảng, Bộ Fragment chung & Cổng Khách thuê (Lưu Quốc Trụ phụ trách chính)
9. **Khung giao diện dùng chung & Điều hướng phân quyền:** Xây dựng Sidebar, Navbar, Footer, Header responsive bằng Bootstrap 5.3 CDN và Thymeleaf Layout Fragments; hiển thị menu tương ứng theo Session vai trò.  
   *👉 Người phụ trách chính: Lưu Quốc Trụ*
10. **Xây dựng bộ Thymeleaf Fragment dùng lại (Reusable Components):** Xây dựng các component chuẩn gồm: thanh tìm kiếm, thanh phân trang, tiêu đề cột sắp xếp, modal xác nhận trước khi xóa, flash alert và thông báo lỗi cạnh ô nhập liệu để bàn giao cho TV1, TV2, TV5.  
    *👉 Người phụ trách chính: Lưu Quốc Trụ*
11. **Cổng tra cứu phòng trống & Xem chi tiết phòng trực tuyến:** Hiện thực trọn gói (Controller, Service, View) cho khách xem danh sách phòng trống, lọc theo giá, diện tích, xem ảnh phòng trực quan.  
    *👉 Người phụ trách chính: Lưu Quốc Trụ*
12. **Cổng thông tin khách thuê trực tuyến (Hợp đồng & Hóa đơn):** Hiện thực trọn gói (Controller, Service, View) cho khách thuê đăng nhập xem thông tin hợp đồng hiện tại và tra cứu chi tiết lịch sử hóa đơn các tháng.  
    *👉 Người phụ trách chính: Lưu Quốc Trụ*
13. **Giao diện gửi phản ánh / yêu cầu sửa chữa:** Hiện thực trọn gói form trực tuyến để khách thuê gửi yêu cầu hỏng hóc thiết bị, chọn mức độ khẩn cấp kèm mô tả chi tiết.  
    *👉 Người phụ trách chính: Lưu Quốc Trụ*

#### Phân hệ 4: Socket Đa Client, Sự cố & Kiểm thử (Đoàn Văn Mẩn phụ trách chính)
14. **Quản lý và điều phối yêu cầu sửa chữa cơ sở vật chất:** Tiếp nhận phiếu phản ánh từ khách thuê, phân công thợ sửa chữa (`tho_sua_chua`), cập nhật chi phí khắc phục (`chi_phi`) và tiến độ xử lý.  
    *👉 Người phụ trách chính: Đoàn Văn Mẩn*
15. **Máy chủ Socket Server đa luồng (Multi-client Socket Server):** Lắng nghe cổng riêng (Port 9999), quản lý kết nối bằng Thread Pool (`ExecutorService`), phục vụ tối thiểu 3 Client kết nối cùng lúc; khởi động qua `CommandLineRunner`, nhận Service qua Dependency Injection; hỗ trợ lệnh `LIST_ROOM`, `BOOK`, `EXIT`; ghi log tên luồng `Thread.currentThread().getName()` trong `ClientHandler`.  
    *👉 Người phụ trách chính: Đoàn Văn Mẩn*
16. **Ứng dụng Desktop Client độc lập:** Ứng dụng Client console/Swing độc lập trong `client/` dùng cú pháp try-with-resources để kết nối và tương tác với Socket Server.  
    *👉 Người phụ trách chính: Đoàn Văn Mẩn*
17. **Chương trình kiểm thử đồng thời độc lập (Concurrency Test):** Viết chương trình kiểm thử đồng thời độc lập `ConcurrencyBookingTest.java` (mở 10 luồng dùng 10 CCCD khác nhau gửi `BOOK P201` qua Socket, dùng `CountDownLatch`, đúng 1 luồng OK và 9 luồng bị từ chối).  
    *👉 Người phụ trách chính: Đoàn Văn Mẩn*

#### Phân hệ 5: Chỉ số Điện Nước, Hóa đơn, Thu ngân & Dashboard Thống kê (Trương Đỗ Minh Quân phụ trách chính)
18. **Quản lý chỉ số điện - nước:** Chốt chỉ số điện nước đầu kỳ và cuối kỳ cho từng phòng theo tháng; tự động tính lượng tiêu thụ thực tế; tích hợp bộ fragment của TV3.  
    *👉 Người phụ trách chính: Trương Đỗ Minh Quân*
19. **Lập và quản lý hóa đơn tiền phòng hàng tháng, Thu ngân & In hóa đơn:** Tự động tổng hợp tiền phòng, tiền điện, tiền nước, phí dịch vụ; kiểm soát ràng buộc duy nhất `UNIQUE(hop_dong_id, thang_nam)`; hiện thực màn hình thu ngân xác nhận thanh toán (gạch nợ) và xuất mẫu in hóa đơn chi tiết.  
    *👉 Người phụ trách chính: Trương Đỗ Minh Quân*
20. **Dashboard thống kê doanh thu & Báo cáo tổng quan (Backend + View):** Tự viết tầng Service truy vấn số liệu tổng hợp và giao diện Thymeleaf hiển thị biểu đồ doanh thu theo tháng, tỷ lệ phòng trống/đang thuê, nợ đọng tiền phòng.  
    *👉 Người phụ trách chính: Trương Đỗ Minh Quân*

---

### 3. Quy chuẩn giao diện bắt buộc cho toàn bộ các trang danh sách
Mọi màn hình danh sách (Phòng trọ, Khách thuê, Hợp đồng, Hóa đơn, Sự cố) đều phải tuân thủ nghiêm ngặt 4 tiêu chuẩn sau thông qua việc tích hợp bộ fragment của TV3:
- **Tìm kiếm, Phân trang, Sắp xếp (Search, Pagination, Sort):** Hỗ trợ tìm kiếm từ khóa, lọc theo trạng thái, phân trang bằng `Pageable` của Spring Data JPA, sắp xếp linh hoạt theo các cột dữ liệu.
- **Hộp thoại xác nhận trước khi xóa (Confirm Dialog):** Modal xác nhận trước khi thực hiện hành động xóa để ngăn chặn việc người dùng thao tác nhầm lẫn.
- **Thông báo lỗi cạnh ô nhập (Inline Field Errors):** Khi dữ liệu nhập không hợp lệ, thông báo lỗi màu đỏ phải hiển thị ngay bên dưới ô nhập liệu tương ứng.
- **Thông báo thành công sau khi lưu (Flash Message):** Sau mỗi thao tác Thêm / Sửa / Xóa thành công, hiển thị Alert thông báo màu xanh lá trên đầu trang qua `RedirectAttributes`.

---

### 4. Quy tắc ràng buộc toàn vẹn khi xóa dữ liệu (Delete Integrity Rules)
Hệ thống phải kiểm tra nghiệp vụ và chặn xóa dữ liệu có ràng buộc, hiển thị thông báo lỗi thân thiện thay vì để văng lỗi cơ sở dữ liệu:
- **Chặn xóa phòng trọ:** Không cho phép xóa phòng đang có hợp đồng (`hop_dong`), đang có yêu cầu sửa chữa chưa xử lý, **hoặc đang có bản ghi đặt chỗ (`dat_cho`) ở trạng thái `DANG_GIU`**. Thông báo: *"Không thể xóa phòng [Mã phòng] do đang có hợp đồng, yêu cầu sửa chữa hoặc đang có khách đặt giữ chỗ. Vui lòng kiểm tra lại."*
- **Chặn xóa khách thuê:** Không cho phép xóa khách thuê đang đứng tên hợp đồng thuê phòng hiệu lực, đang có tài khoản liên kết, **hoặc đang có bản ghi đặt chỗ (`dat_cho`) ở trạng thái `DANG_GIU`**. Thông báo: *"Không thể xóa khách thuê do đang có hợp đồng hoặc đang đặt giữ chỗ phòng."*
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
- **Người phụ trách:** Bùi Trần Tấn Tài
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
- **Người phụ trách:** Bùi Trần Tấn Tài
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
- **Người phụ trách:** Trương Đỗ Minh Quân
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
- **Người phụ trách:** Đoàn Văn Mẩn
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
4. **Phòng có trạng thái `DA_DAT`** thì phải có bản ghi tương ứng trong `dat_cho` với trạng thái `DANG_GIU` và `han_giu_cho` còn hiệu lực. Trong script dữ liệu mẫu, cột `han_giu_cho` của các bản ghi `DANG_GIU` **bắt buộc dùng `DATEADD(day, 7, GETDATE())`** để đảm bảo thời hạn giữ chỗ luôn còn hiệu lực trong 7 ngày khi chạy demo và kiểm thử.
5. **Mật khẩu trong `tai_khoan`** phải được băm sẵn bằng SHA-256 + salt tương thích chính xác với thuật toán giải mã của Service xác thực.

---

## IV. ĐỀ XUẤT VÀ HIỆN THỰC XỬ LÝ ĐỒNG THỜI VÀ ĐA LUỒNG

Trong đề tài này, nhóm hiện thực **hai cơ chế đa luồng & đồng thời** trọng tâm:
1. **Cơ chế 1: Khóa bi quan (`findByIdForUpdate`) chống thuê / đặt trùng phòng (Concurrency Lock)**
2. **Cơ chế 2: Tác vụ nền định kỳ (`@Scheduled`) tự động giải phóng phòng giữ chỗ quá hạn (Background Scheduled Task)**

---

### 1. Cơ chế 1: Chống đặt/thuê trùng phòng bằng Pessimistic Lock
- **Người phụ trách chính:** **Bùi Trần Tấn Tài**
- **Nghiệp vụ:** Khi có hai hoặc nhiều yêu cầu đặt giữ chỗ / tạo hợp đồng cho cùng một phòng trọ (ví dụ phòng `P201`) gửi đến đồng thời từ Web hoặc Socket Server, hệ thống chỉ cho phép một yêu cầu thành công, các yêu cầu còn lại bị từ chối và nhận thông báo lỗi thích hợp.
- **Giải pháp kỹ thuật:** Cả phương thức xử lý tạo hợp đồng trên Web và lệnh `BOOK` của Socket Server đều gọi chung qua tầng Service, sử dụng phương thức truy vấn khóa bi quan (**Pessimistic Write Lock**) trong `PhongTroRepository`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM PhongTro p WHERE p.id = :id")
Optional<PhongTro> findByIdForUpdate(@Param("id") Integer id);
```

- **Quy trình chuẩn trong Transaction của Lệnh `BOOK`:**
  1. Bắt đầu giao dịch `@Transactional`.
  2. **Khóa phòng trước:** Gọi `findByIdForUpdate(phongId)` để khóa dòng dữ liệu phòng trọ trên SQL Server (`SELECT ... WITH (UPDLOCK, ROWLOCK)`).
  3. **Kiểm tra trạng thái phòng:** Kiểm tra xem trạng thái có phải là `TRONG` không. Nếu phòng không còn `TRONG` (ví dụ đã bị đặt hoặc đang thuê), lập tức dừng giao dịch và ném ngoại lệ nghiệp vụ (`RoomAlreadyBookedException`) hoặc trả về phản hồi `FAIL`.
  4. **Tìm hoặc tạo khách thuê:** Sau khi đã giữ khóa phòng an toàn, hệ thống mới tìm hồ sơ khách thuê theo CCCD; nếu chưa có trong CSDL thì tạo mới bản ghi `khach_thue`.
  5. **Tạo bản ghi giữ chỗ:** Tạo bản ghi mới trong bảng `dat_cho` với trạng thái `DANG_GIU` và gán thời hạn `han_giu_cho`; đồng thời chuyển trạng thái phòng `phong_tro.trang_thai = 'DA_DAT'`.
  6. **Commit transaction:** Dữ liệu được ghi nhận toàn vẹn vào SQL Server và giải phóng khóa. Toàn bộ chuỗi thao tác trên thực hiện gói gọn trong **cùng một Transaction** để bảo đảm tính nguyên tử (Atomicity).

---

### 2. Bộ kiểm thử đồng thời độc lập (`ConcurrencyBookingTest.java`) và Script Reset
- **Người phụ trách:** **Đoàn Văn Mẩn**
- **Yêu cầu kỹ thuật:** Viết thành một ứng dụng Client độc lập chạy bằng hàm `main()`, thuần chuẩn Java Core (`java.util.concurrent`, `Socket`), **hoàn toàn không sử dụng thêm bất kỳ thư viện ngoài nào khác**.
- **Cơ chế kiểm thử:**
  - Khởi tạo Thread Pool gồm 10 luồng: `ExecutorService pool = Executors.newFixedThreadPool(10);`
  - Chuẩn bị dữ liệu mô phỏng: **10 khách thuê với 10 số CCCD hoàn toàn khác nhau** (từ `079204000001` đến `079204000010`) để mô phỏng chính xác 10 người dùng độc lập thực tế, tránh va chạm Unique CCCD.
  - Sử dụng hai biến đếm `CountDownLatch readyLatch = new CountDownLatch(10);` và `CountDownLatch startLatch = new CountDownLatch(1);` để ép 10 luồng kết nối Socket sẵn sàng và đồng loạt kích hoạt gửi lệnh `BOOK P201#<Họ tên>#<CCCD_i>#<SĐT>` tới cổng 9999 tại cùng một thời điểm.
  - Ghi nhận kết quả: Chính xác **1 luồng nhận phản hồi OK** (đặt phòng thành công) và **9 luồng nhận phản hồi từ chối / lỗi** do phòng đã bị khóa và chuyển trạng thái `DA_DAT`.
- **Script chuẩn bị dữ liệu kiểm thử (`sql/reset_p201.sql`):**
  - Để đảm bảo bài kiểm thử có thể chạy lặp lại nhiều lần với kết quả nhất quán, nhóm xây dựng script `sql/reset_p201.sql` nhằm xóa các bản ghi `dat_cho` liên quan đến `P201` và đưa phòng `P201` về trạng thái `TRONG` trước mỗi lần chạy test:
    ```sql
    DELETE FROM dat_cho WHERE phong_id IN (SELECT id FROM phong_tro WHERE ma_phong = 'P201');
    UPDATE phong_tro SET trang_thai = 'TRONG' WHERE ma_phong = 'P201';
    ```
- Toàn bộ nhật ký (log) kiểm thử và bảng số liệu đối soát sẽ được đưa vào **Chương 4 của Báo cáo đồ án**.

---

### 3. Cơ chế 2: Tác vụ nền tự động giải phóng đặt chỗ quá hạn (@Scheduled)
- **Người phụ trách chính:** **Bùi Trần Tấn Tài**
- **Nghiệp vụ:** Khách hàng giữ chỗ phòng chỉ được giữ tối đa trong một khoảng thời gian (ví dụ: 24 giờ kể từ thời điểm đặt). Nếu quá thời hạn này mà khách không đến làm hợp đồng thuê chính thức, phòng phải được tự động thu hồi về trạng thái `TRONG` để khách khác có thể đặt.
- **Giải pháp kỹ thuật chi tiết:**
  - Bật tính năng lập lịch chạy nền của Spring bằng `@EnableScheduling`.
  - **Khắc phục lỗi Spring AOP Proxy (Self-Invocation):** Trong Spring Framework, cơ chế `@Transactional` và khóa bi quan được quản lý qua Dynamic Proxy bao bọc bên ngoài Bean. Nếu phương thức `@Transactional` được gọi trực tiếp nội bộ trong cùng một class (qua con trỏ `this`), cuộc gọi sẽ bỏ qua Spring Proxy, dẫn đến Transaction không được mở và khóa không hoạt động. Do đó, thiết kế bắt buộc phải tách riêng: lớp lập lịch `DatChoScheduler` tiêm phụ thuộc `DatChoService`, và phương thức xử lý transaction `releaseSingleBooking` nằm tại `DatChoService`.

```java
// Lớp lập lịch định kỳ
@Component
public class DatChoScheduler {
    private static final Logger log = LoggerFactory.getLogger(DatChoScheduler.class);

    @Autowired
    private DatChoRepository datChoRepository;

    @Autowired
    private DatChoService datChoService; // Inject Service để đảm bảo đi qua Spring Proxy

    @Scheduled(fixedDelay = 60000)
    public void autoReleaseExpiredBookings() {
        List<DatCho> expiredList = datChoRepository.findExpiredBookings(LocalDateTime.now());
        for (DatCho dc : expiredList) {
            // Gọi qua Spring Bean DatChoService để @Transactional và findByIdForUpdate hoạt động
            datChoService.releaseSingleBooking(dc.getId(), dc.getPhongTro().getId());
        }
    }
}
```

```java
// Lớp Service quản trị giao dịch độc lập
@Service
public class DatChoService {
    private static final Logger log = LoggerFactory.getLogger(DatChoService.class);

    @Autowired
    private PhongTroRepository phongTroRepository;

    @Autowired
    private DatChoRepository datChoRepository;

    @Transactional
    public void releaseSingleBooking(Integer datChoId, Integer phongId) {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Đang kiểm tra và giải phóng phòng ID: {}", threadName, phongId);

        // 1. Khóa phòng trọ cần giải phóng bằng Pessimistic Lock
        PhongTro phong = phongTroRepository.findByIdForUpdate(phongId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phòng"));

        // 2. Kiểm tra lại trong transaction: đảm bảo bản ghi dat_cho vẫn còn là DANG_GIU
        DatCho datCho = datChoRepository.findById(datChoId).orElse(null);
        if (datCho != null && "DANG_GIU".equals(datCho.getTrangThai())) {
            datCho.setTrangThai("HET_HAN");
            phong.setTrangThai("TRONG");
            log.info("[{}] Đã giải phóng phòng {} về trạng thái TRONG", threadName, phong.getMaPhong());
        }
    }
}
```

- **Yêu cầu ghi log tên luồng (`Thread.currentThread().getName()`):** Trong tác vụ `@Scheduled`, mọi dòng log phải in rõ tên luồng đang xử lý (ví dụ: `[scheduling-1]`) để chụp màn hình đưa vào báo cáo làm bằng chứng hoạt động của tác vụ đa luồng chạy nền.

---

## V. ĐỀ XUẤT VÀ HIỆN THỰC MẠNG SOCKET (MULTI-CLIENT SOCKET)

- **Người phụ trách chính:** **Đoàn Văn Mẩn**
- **Kiến trúc Server:**
  - `SocketServer` lắng nghe trên cổng riêng biệt `Port 9999` (độc lập với cổng Web `8080`).
  - Khởi động tự động trong vòng đời ứng dụng Spring Boot thông qua việc hiện thực interface `CommandLineRunner` (chạy trên một background thread riêng để không block luồng khởi động Spring).
  - Quản lý các kết nối đến bằng `ExecutorService` (Thread Pool), phục vụ tối thiểu **3 Client kết nối và tương tác cùng lúc**.
  - **Tiêm phụ thuộc (Dependency Injection):** `ClientHandler` được nhận các Spring Service (`DatChoService`, `PhongTroService`) qua cơ chế tiêm phụ thuộc của Spring (không dùng từ khóa `new` Service trong Handler). Điều này giúp bảo toàn ngữ cảnh Spring Container, bảo đảm cơ chế quản lý giao dịch `@Transactional` và khóa bi quan `findByIdForUpdate` hoạt động chuẩn xác trong từng luồng Socket.
  - **Ghi log tên luồng trong ClientHandler:** Mọi thao tác kết nối, nhận gói tin, gọi Service và phản hồi kết quả đều phải in rõ `Thread.currentThread().getName()` (ví dụ: `[pool-2-thread-1] Đang xử lý lệnh BOOK từ Client...`) để chụp màn hình minh chứng cho báo cáo đồ án.
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
  - **Lệnh tra cứu phòng:** `LIST_ROOM` — Server trả về danh sách các phòng trống.
  - **Lệnh đặt giữ chỗ:** `BOOK <ma_phong>#<ho_ten>#<cccd>#<sdt>` — Server gọi Service có `findByIdForUpdate` khóa phòng trước, tìm/tạo khách sau, lưu `dat_cho`, chuyển phòng sang `DA_DAT` và phản hồi: `OK#Dat cho thanh cong` hoặc `FAIL#Phong da duoc dat hoac dang thue`.
  - **Lệnh ngắt kết nối:** `EXIT` — Client gửi thông báo ngắt kết nối, Server phản hồi `BYE#Tam biet` và đóng socket giải phóng tài nguyên một cách an toàn.

---

## VI. QUY TẮC LÀM VIỆC TRÊN GIT CỦA NHÓM

Để minh bạch tiến độ và bảo đảm điểm số cá nhân khi giảng viên kiểm tra nhật ký Git:
1. **Quy tắc phân nhánh:**
   - Nhánh `main`: Nhánh ổn định cao nhất, tuyệt đối **không ai được push trực tiếp vào `main`**.
   - Nhánh `dev`: Nhánh phát triển chung để tích hợp mã nguồn của cả nhóm.
   - Nhánh tính năng cá nhân của 5 thành viên:
     - Nguyễn Minh Tâm (TV1): `feature/db-entity-tam`
     - Bùi Trần Tấn Tài (TV2): `feature/contract-booking`
     - Lưu Quốc Trụ (TV3): `feature/ui-thymeleaf`
     - Đoàn Văn Mẩn (TV4): `feature/socket-network`
     - Trương Đỗ Minh Quân (TV5): `feature/billing-dashboard`
2. **Cam kết đóng góp (Commit Rules):**
   - Mỗi thành viên bắt buộc sử dụng **tài khoản GitHub cá nhân** của mình để commit đều đặn trong suốt quá trình làm đồ án.
   - Nội dung commit phải rõ ràng theo chuẩn Conventional Commits (Ví dụ: `feat: tao bang dat cho va scheduled task`, `test: viet chuong trinh kiem thu concurrency`).
3. **Quy trình gộp mã nguồn (Pull Request & Code Review):**
   - Trước khi code: Kéo code mới nhất từ `dev` về (`git pull origin dev`).
   - Sau khi hoàn thành và tự test trên máy: Đẩy nhánh tính năng lên GitHub và mở **Pull Request (PR)** gộp vào `dev`.
   - Phải có ít nhất một thành viên khác xem xét (Code Review) và phê duyệt trước khi merge vào `dev`.

---

## VII. CÁC ĐIỂM CẦN HỎI VÀ XIN Ý KIẾN GIẢNG VIÊN

1. **Về hình thức nộp và lịch bảo vệ:** Hạn nộp báo cáo chính thức và ngày thuyết trình/vấn đáp đồ án rơi vào tuần thứ mấy của học kỳ?
2. **Về kho lưu trữ GitHub:** Kho lưu trữ mã nguồn của nhóm cần để chế độ Public hay để Private rồi mời tài khoản GitHub của giảng viên vào (Collaborator)?
3. **Về phạm vi đa luồng:** Phần giữ chỗ tự hết hạn bằng tác vụ nền `@Scheduled` có được tính là phần đa luồng thứ hai của đồ án không?
4. **Về triển khai Socket Server:** Giảng viên yêu cầu Socket Server khởi động cùng ứng dụng Spring Boot thông qua `CommandLineRunner` (lắng nghe cổng phụ 9999) hay chạy dưới dạng một ứng dụng console Java độc lập hoàn toàn?
5. **Về phương thức kiểm thử Concurrency:** Giảng viên mong muốn nhóm chứng minh phần Concurrency bằng chương trình chạy hàm `main()` độc lập in log ra màn hình (`ConcurrencyBookingTest.java`), hay có thể demo trực tiếp trên 2 trình duyệt bấm nút cùng lúc?
6. **Về định dạng báo cáo:** Cuốn báo cáo nộp file mềm PDF qua hệ thống LMS hay yêu cầu in đóng cuốn bìa kiếng, và slide thuyết trình có mẫu template bắt buộc riêng của khoa hay không?
