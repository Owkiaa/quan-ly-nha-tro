# Kế Hoạch Chia Việc Nâng Cấp Repo Cho 4 Thành Viên (Đợt Hoàn Thiện Đồ Án)

> Tài liệu giao việc chính thức trên nhánh `dev` — Trưởng nhóm: Nguyễn Minh Tâm.
> Mục tiêu: nâng chất lượng Backend, Java Socket Desktop Client, bộ kiểm thử tự động (JUnit 5 + Mockito), GitHub Actions CI và tài liệu kỹ thuật.

---

## I. Nguyên tắc bắt buộc (đọc kỹ trước khi code)

> [!IMPORTANT]
> 1. **CẤM chỉnh sửa bất kỳ file nào trong `src/main/resources/templates/**` và `src/main/resources/static/**`.**
>    Giao diện Web do **Nguyễn Minh Tâm** thiết kế và kiểm thử. Trước khi `git add`, **bắt buộc chạy `git status`** để chắc chắn không dính file giao diện.
> 2. **Tương thích ngược 100%:** tham số `@RequestParam` thêm mới phải `required = false`; giữ nguyên tên các thuộc tính `Model` cũ để web luôn chạy ổn định.
> 3. **Quy tắc Git:**
>    ```bash
>    git checkout dev
>    git pull origin dev
>    git checkout -b feature/<ten>-upgrade
>    ```
>    * Dùng **đúng tài khoản/email GitHub cá nhân** để commit được ghi nhận vào Contributors.
>    * Không gộp tất cả vào 1 commit. Mỗi đầu việc xong, chạy `.\mvnw.cmd test` thấy `BUILD SUCCESS` thì commit riêng theo Conventional Commits (`fix(...)`, `feat(...)`, `test(...)`, `docs(...)`).
>    * Xong thì push nhánh và **mở Pull Request vào `dev`**. **Không push trực tiếp vào `main` hoặc `dev`.**

---

## II. Bảng giao việc

### 1. Bùi Trần Tấn Tài (TV2 – 2001240413) — nhánh `feature/tai-upgrade`
Mục tiêu: tối thiểu **6–8 commits**.

File được sửa/tạo:
- `src/main/java/com/nhatro/controller/DatChoController.java`
- `src/main/java/com/nhatro/controller/HopDongController.java`
- `src/main/java/com/nhatro/service/DatChoService.java`, `DatChoScheduler.java`, `HopDongService.java`
- `src/test/java/com/nhatro/service/DatChoServiceTest.java` *(mới)*
- `src/test/java/com/nhatro/service/HopDongServiceTest.java` *(mới)*
- `docs/CONCURRENCY_AND_SCHEDULER.md` *(mới)*

Checklist:
- [ ] Chống lỗi 500 khi `sort`: thêm `ALLOWED_SORT_FIELDS` cho `DatChoController` và `HopDongController`; tên cột lạ thì fallback `id,desc`.
- [ ] Sửa số Tổng KPI độc lập bộ lọc: thêm `totalBookings` (đặt chỗ) và `totalContracts` (hợp đồng) vào `Model`.
- [ ] Quét hợp đồng hết hạn: hàm định kỳ chuyển `HIEU_LUC` có `ngayKetThuc < hôm nay` sang `HET_HAN` (log `Thread.currentThread().getName()`); chặn xóa hợp đồng đã có hóa đơn với thông báo chuẩn.
- [ ] Unit Test (JUnit 5 + Mockito, không cần SQL Server):
  - Đặt chỗ phòng `TRONG` → `DA_DAT`; chặn đặt trùng (`RoomAlreadyBookedException`); hủy giữ chỗ trả về `TRONG`; `releaseSingleBooking` quá hạn.
  - Lập hợp đồng từ `TRONG`; từ `DA_DAT` đúng khách vs sai khách giữ chỗ; gia hạn sai ngày; thanh lý trả phòng `TRONG` và khách `DA_TRA_PHONG`.
- [ ] Viết `docs/CONCURRENCY_AND_SCHEDULER.md`: `PESSIMISTIC_WRITE` (`findByIdForUpdate`), lý do tách `DatChoScheduler`/`DatChoService` (AOP self-invocation), 2 sơ đồ Sequence (Mermaid).

### 2. Lưu Quốc Trụ (TV3 – 2001240521) — nhánh `feature/tru-upgrade`
Mục tiêu: tối thiểu **6–8 commits**. **Không sửa file `.html`/`.css`** — chỉ Backend + Test cho Cổng Khách thuê.

File được sửa/tạo:
- `src/main/java/com/nhatro/controller/TenantPortalController.java`
- `src/main/java/com/nhatro/service/TenantPortalService.java`
- `src/main/java/com/nhatro/repository/PhongTroRepository.java` *(chỉ thêm query, giữ hàm cũ)*
- `src/main/java/com/nhatro/dto/TenantRegisterDTO.java` *(mới)*
- `src/test/java/com/nhatro/service/TenantPortalServiceTest.java` *(mới)*
- `docs/TENANT_PORTAL_SPEC.md` *(mới)*

Checklist:
- [ ] Lọc nâng cao phòng trống: `keyword`, `loaiPhong`, `giaTu`, `giaDen`, `dienTichToiThieu` (đều nullable); `TenantPortalController.rooms(...)` nhận thêm `@RequestParam(required = false)` và đẩy lại ra `Model`.
- [ ] Đặt giữ chỗ trực tuyến: `bookRoomForTenant(khachId, phongId, soGioGiuCho, ghiChu)` — tối đa 2 phòng `DANG_GIU`/khách, gọi `datChoService.datChoPhong(...)` (khóa bi quan); `getBookingsByKhach`, `cancelBookingByKhach` (kiểm tra đúng chủ sở hữu, chống IDOR); endpoint `POST /tenant/rooms/{id}/book`, `POST /tenant/bookings/{id}/cancel`.
- [ ] Tự đăng ký tài khoản khách thuê: `TenantRegisterDTO` + `registerTenantAccount` (validate CCCD 9–12 số, SĐT `0xxxxxxxxx`, email, mật khẩu ≥ 6 ký tự & khớp, trùng tên đăng nhập; băm bằng `PasswordUtil.hash`; liên kết/tạo `KhachThue` trong 1 `@Transactional`).
- [ ] Unit Test: lọc phòng, chặn giữ quá 2 phòng, cô lập dữ liệu giữa các khách, báo sự cố chỉ khi có hợp đồng `HIEU_LUC`.
- [ ] Viết `docs/TENANT_PORTAL_SPEC.md`: luồng nghiệp vụ và cơ chế cô lập dữ liệu theo `Session khachId`.

### 3. Đoàn Văn Mẩn (TV4 – 2001240270) — nhánh `feature/man-upgrade`
Mục tiêu: tối thiểu **7–9 commits**.

File được sửa/tạo:
- `src/main/java/com/nhatro/socket/ClientHandler.java`
- `client/src/com/nhatro/client/SocketClientApp.java`
- `client/src/com/nhatro/client/SocketClientSwingApp.java` *(mới)*
- `client/README.md`
- `src/main/java/com/nhatro/controller/YeuCauSuaChuaController.java`
- `src/main/java/com/nhatro/service/YeuCauSuaChuaService.java`, `service/impl/YeuCauSuaChuaServiceImpl.java`
- `src/main/java/com/nhatro/repository/YeuCauSuaChuaRepository.java`
- `src/test/java/com/nhatro/service/YeuCauSuaChuaServiceTest.java` *(mới)*
- `src/test/java/com/nhatro/socket/ClientHandlerTest.java` *(mới)*
- `docs/SOCKET_PROTOCOL.md` *(mới)*

Checklist:
- [ ] Chuẩn hóa giao thức Socket: giữ tương thích 100% với `ConcurrencyBookingTest` (`BOOK` → `OK#`/`FAIL#`, `EXIT` → `BYE#`); thêm `ROOM_DETAIL <ma_phong>` và `PING` (`PONG#Server online`); đọc đa dòng `LIST_ROOM` không dùng `Thread.sleep(50)`; log `[ThreadName]`.
- [ ] Nâng cấp `SocketClientApp.java`: menu số (1 Danh sách phòng trống, 2 Chi tiết phòng, 3 Đặt giữ chỗ có validate CCCD/SĐT, 4 Gõ lệnh thô, 0 Thoát).
- [ ] Xây `SocketClientSwingApp.java` (Swing, thuần JDK 17): Host/Port + Kết nối/Ngắt, `JTable` phòng trống, click dòng tự điền mã phòng vào form đặt giữ chỗ, khung log gói tin.
- [ ] Hoàn thiện Backend Sự cố: `getById` ném `ResourceNotFoundException` (404); chặn phân công/nghiệm thu lại phiếu `HOAN_THANH`; kiểm tra tồn tại trước khi xóa; thêm KPI (`countByTrangThai`, `countByMucDo`, `sumTongChiPhi`) đưa vào `Model` ở `index`.
- [ ] Unit Test `YeuCauSuaChuaServiceTest` và `ClientHandlerTest` (Mock Streams).
- [ ] Viết `docs/SOCKET_PROTOCOL.md` và cập nhật `client/README.md` (bảng lệnh, sơ đồ đa luồng `ExecutorService`, hướng dẫn demo nhiều client).

### 4. Trương Đỗ Minh Quân (TV5 – 2001240390) — nhánh `feature/quan-upgrade`
Mục tiêu: tối thiểu **8–10 commits** (đẩy đều để cân bằng lịch sử đóng góp).

File được sửa/tạo:
- `src/main/java/com/nhatro/controller/HoaDonController.java`
- `src/main/java/com/nhatro/service/HoaDonService.java`
- `src/main/java/com/nhatro/repository/HoaDonRepository.java`
- `src/main/java/com/nhatro/service/DashboardService.java`
- `src/main/java/com/nhatro/dto/DashboardDTO.java`
- `src/test/java/com/nhatro/service/HoaDonServiceTest.java` *(mới)*
- `src/test/java/com/nhatro/service/DashboardServiceTest.java` *(mới)*
- `.github/workflows/ci.yml` *(mới)*
- `docs/DATABASE_AND_BILLING.md` *(mới)*

Checklist:
- [ ] Bảo mật `HoaDonController`: chặn Open Redirect ở `xacNhanThanhToan`/`huyThanhToan` (chỉ nhận `redirectUrl` bắt đầu bằng `/` và không bắt đầu bằng `//`); thêm `ALLOWED_SORT_FIELDS` (`id`, `maHoaDon`, `thangNam`, `tongTien`, `trangThai`, `ngayLap`); đưa `totalInvoices` và `tongNoChuaThu` vào `Model`; `formTaoMoi` nhận `@RequestParam(required = false) Integer hopDongId` để điền sẵn hợp đồng, tiền phòng, chỉ số điện/nước cũ.
- [ ] Xóa hóa đơn nháp an toàn: `POST /hoa-don/{id}/xoa` chỉ cho `CHUA_THANH_TOAN`; chặn `DA_THANH_TOAN` bằng thông báo nghiệp vụ rõ ràng.
- [ ] `DashboardService`/`DashboardDTO`: thêm `@Transactional(readOnly = true)`, `tongChiPhiSuaChua`, `loiNhuanThucNhan = tongDoanhThuDaThu - tongChiPhiSuaChua` (giữ nguyên getter cũ).
- [ ] Unit Test `HoaDonServiceTest` (tính tiền, chỉ số mới < cũ, số âm, trùng tháng, hợp đồng đã thanh lý, gạch nợ, chặn xóa hóa đơn đã thanh toán) và `DashboardServiceTest` (tỷ lệ lấp đầy khi tổng phòng = 0, biểu đồ 6 tháng, lợi nhuận).
- [ ] Tạo `.github/workflows/ci.yml`: JDK 17, `mvn test-compile test`, biên dịch `client/src/com/nhatro/client/*.java`, chạy khi push/PR vào `dev` và `main`.
- [ ] Viết `docs/DATABASE_AND_BILLING.md`: ERD 7 bảng (Mermaid), công thức điện nước, ràng buộc duy nhất, quy trình thu ngân.
