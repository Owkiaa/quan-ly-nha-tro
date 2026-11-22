-- ==============================================================
-- SCRIPT 02: TẠO CẤU TRÚC BẢNG (SCHEMA) VÀ DỮ LIỆU MẪU (SQL SERVER)
-- ==============================================================

USE NhaTroDB;
GO

-- 1. XÓA CÁC BẢNG NẾU ĐÃ TỒN TẠI (ĐÚNG THỨ TỰ RÀNG BUỘC KHÓA NGOẠI)
IF OBJECT_ID('yeu_cau_sua_chua', 'U') IS NOT NULL DROP TABLE yeu_cau_sua_chua;
IF OBJECT_ID('hoa_don', 'U') IS NOT NULL DROP TABLE hoa_don;
IF OBJECT_ID('hop_dong', 'U') IS NOT NULL DROP TABLE hop_dong;
IF OBJECT_ID('dat_cho', 'U') IS NOT NULL DROP TABLE dat_cho;
IF OBJECT_ID('tai_khoan', 'U') IS NOT NULL DROP TABLE tai_khoan;
IF OBJECT_ID('khach_thue', 'U') IS NOT NULL DROP TABLE khach_thue;
IF OBJECT_ID('phong_tro', 'U') IS NOT NULL DROP TABLE phong_tro;
IF OBJECT_ID('he_thong', 'U') IS NOT NULL DROP TABLE he_thong;
GO

-- ==============================================================
-- 2. TẠO 7 BẢNG CHUẨN THEO THIẾT KẾ
-- ==============================================================

-- Bảng 1: phong_tro (Không có cột version)
CREATE TABLE phong_tro (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phong VARCHAR(20) NOT NULL UNIQUE,
    ten_phong NVARCHAR(50) NOT NULL,
    loai_phong NVARCHAR(30) NULL,
    dien_tich FLOAT NULL,
    gia_thue DECIMAL(12,2) NOT NULL,
    hinh_anh VARCHAR(255) NULL,
    trang_thai VARCHAR(20) NOT NULL,
    mo_ta NVARCHAR(255) NULL,
    CONSTRAINT CK_phong_tro_trang_thai CHECK (trang_thai IN ('TRONG', 'DA_DAT', 'DA_THUE', 'BAO_TRI'))
);
GO

-- Bảng 2: khach_thue
CREATE TABLE khach_thue (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ho_ten NVARCHAR(100) NOT NULL,
    cccd VARCHAR(20) NOT NULL UNIQUE,
    so_dien_thoai VARCHAR(15) NOT NULL,
    email VARCHAR(100) NULL,
    ngay_sinh DATE NULL,
    que_quan NVARCHAR(150) NULL,
    gioi_tinh NVARCHAR(10) NULL,
    bien_so_xe VARCHAR(20) NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'DANG_THUE',
    CONSTRAINT CK_khach_thue_trang_thai CHECK (trang_thai IN ('DANG_THUE', 'DA_TRA_PHONG'))
);
GO

-- Bảng 3: tai_khoan
CREATE TABLE tai_khoan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    mat_khau VARCHAR(100) NOT NULL,
    vai_tro VARCHAR(20) NOT NULL,
    khach_id INT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    ngay_tao DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_tai_khoan_khach_thue FOREIGN KEY (khach_id) REFERENCES khach_thue(id),
    CONSTRAINT CK_tai_khoan_vai_tro CHECK (vai_tro IN ('ADMIN', 'STAFF', 'TENANT'))
);
GO

-- Bảng 4: dat_cho
CREATE TABLE dat_cho (
    id INT IDENTITY(1,1) PRIMARY KEY,
    phong_id INT NOT NULL,
    khach_id INT NOT NULL,
    ngay_dat DATETIME NOT NULL DEFAULT GETDATE(),
    han_giu_cho DATETIME NOT NULL,
    trang_thai VARCHAR(30) NOT NULL,
    ghi_chu NVARCHAR(255) NULL,
    CONSTRAINT FK_dat_cho_phong_tro FOREIGN KEY (phong_id) REFERENCES phong_tro(id),
    CONSTRAINT FK_dat_cho_khach_thue FOREIGN KEY (khach_id) REFERENCES khach_thue(id),
    CONSTRAINT CK_dat_cho_trang_thai CHECK (trang_thai IN ('DANG_GIU', 'HET_HAN', 'DA_CHUYEN_HOP_DONG'))
);
GO

-- Bảng 5: hop_dong
CREATE TABLE hop_dong (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_hop_dong VARCHAR(30) NOT NULL UNIQUE,
    phong_id INT NOT NULL,
    khach_id INT NOT NULL,
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL,
    tien_dat_coc DECIMAL(12,2) NOT NULL,
    gia_thue_thoa_thuan DECIMAL(12,2) NOT NULL,
    chu_ky_thanh_toan INT NOT NULL DEFAULT 1,
    trang_thai VARCHAR(20) NOT NULL,
    CONSTRAINT FK_hop_dong_phong_tro FOREIGN KEY (phong_id) REFERENCES phong_tro(id),
    CONSTRAINT FK_hop_dong_khach_thue FOREIGN KEY (khach_id) REFERENCES khach_thue(id),
    CONSTRAINT CK_hop_dong_trang_thai CHECK (trang_thai IN ('HIEU_LUC', 'HET_HAN', 'DA_THANH_LY'))
);
GO

-- Bảng 6: hoa_don
CREATE TABLE hoa_don (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_hoa_don VARCHAR(30) NOT NULL UNIQUE,
    hop_dong_id INT NOT NULL,
    thang_nam VARCHAR(7) NOT NULL,
    chi_so_dien_cu INT NOT NULL,
    chi_so_dien_moi INT NOT NULL,
    don_gia_dien DECIMAL(10,2) NOT NULL DEFAULT 3500,
    chi_so_nuoc_cu INT NOT NULL,
    chi_so_nuoc_moi INT NOT NULL,
    don_gia_nuoc DECIMAL(10,2) NOT NULL DEFAULT 20000,
    tien_phong DECIMAL(12,2) NOT NULL,
    tien_dien DECIMAL(12,2) NOT NULL,
    tien_nuoc DECIMAL(12,2) NOT NULL,
    tien_dich_vu DECIMAL(12,2) NOT NULL DEFAULT 100000,
    tong_tien DECIMAL(12,2) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL,
    ngay_lap DATETIME NOT NULL DEFAULT GETDATE(),
    ngay_thanh_toan DATETIME NULL,
    CONSTRAINT FK_hoa_don_hop_dong FOREIGN KEY (hop_dong_id) REFERENCES hop_dong(id),
    CONSTRAINT UQ_hoa_don_hop_dong_thang UNIQUE (hop_dong_id, thang_nam),
    CONSTRAINT CK_hoa_don_trang_thai CHECK (trang_thai IN ('CHUA_THANH_TOAN', 'DA_THANH_TOAN'))
);
GO

-- Bảng 7: yeu_cau_sua_chua
CREATE TABLE yeu_cau_sua_chua (
    id INT IDENTITY(1,1) PRIMARY KEY,
    phong_id INT NOT NULL,
    khach_id INT NOT NULL,
    noi_dung NVARCHAR(255) NOT NULL,
    tho_sua_chua NVARCHAR(100) NULL,
    chi_phi DECIMAL(12,2) NOT NULL DEFAULT 0,
    muc_do VARCHAR(20) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL,
    ngay_tao DATETIME NOT NULL DEFAULT GETDATE(),
    ngay_hoan_thanh DATETIME NULL,
    CONSTRAINT FK_sua_chua_phong_tro FOREIGN KEY (phong_id) REFERENCES phong_tro(id),
    CONSTRAINT FK_sua_chua_khach_thue FOREIGN KEY (khach_id) REFERENCES khach_thue(id),
    CONSTRAINT CK_sua_chua_muc_do CHECK (muc_do IN ('BINH_THUONG', 'CAP_BACH')),
    CONSTRAINT CK_sua_chua_trang_thai CHECK (trang_thai IN ('CHO_TIEP_NHAN', 'DANG_SUA', 'HOAN_THANH'))
);
GO

-- ==============================================================
-- 3. CHÈN DỮ LIỆU MẪU NHẤT QUÁN LOGIC
-- ==============================================================

-- 3.1. Dữ liệu mẫu bảng phong_tro (20 phòng từ P101 đến P405)
-- P201: TRONG, không có dat_cho để làm test Concurrency
-- Phòng DA_THUE: 11 phòng; DA_DAT: 3 phòng; BAO_TRI: 2 phòng; TRONG: 4 phòng
INSERT INTO phong_tro (ma_phong, ten_phong, loai_phong, dien_tich, gia_thue, hinh_anh, trang_thai, mo_ta) VALUES
(N'P101', N'Phòng 101', N'Đơn', 20.0, 2200000, '/images/p101.jpg', 'TRONG', N'Phòng tầng 1 thoáng mát, cửa sổ hướng đông'),
(N'P102', N'Phòng 102', N'Đôi', 25.0, 2800000, '/images/p102.jpg', 'DA_THUE', N'Phòng tầng 1 có gác lửng, ban công riêng'),
(N'P103', N'Phòng 103', N'Studio', 30.0, 3500000, '/images/p103.jpg', 'DA_THUE', N'Phòng khép kín đầy đủ tiện nghi, máy lạnh'),
(N'P104', N'Phòng 104', N'Đơn', 18.0, 2000000, '/images/p104.jpg', 'DA_DAT', N'Phòng tầng 1 giá rẻ, gần nhà xe'),
(N'P105', N'Phòng 105', N'Đôi', 28.0, 3200000, '/images/p105.jpg', 'DA_THUE', N'Phòng tầng 1 rộng rãi, có bếp nấu ăn riêng'),
(N'P201', N'Phòng 201', N'Đơn', 22.0, 2400000, '/images/p201.jpg', 'TRONG', N'Phòng tầng 2 yên tĩnh, phục vụ kiểm thử Concurrency'),
(N'P202', N'Phòng 202', N'Đôi', 26.0, 3000000, '/images/p202.jpg', 'DA_THUE', N'Phòng tầng 2 ban công thoáng mát'),
(N'P203', N'Phòng 203', N'Studio', 32.0, 3800000, '/images/p203.jpg', 'DA_THUE', N'Phòng Studio cao cấp có tủ lạnh, điều hòa'),
(N'P204', N'Phòng 204', N'Gác lửng', 24.0, 2700000, '/images/p204.jpg', 'DA_DAT', N'Phòng tầng 2 gác đúc kiên cố'),
(N'P205', N'Phòng 205', N'Đôi', 27.0, 3100000, '/images/p205.jpg', 'DA_THUE', N'Phòng tầng 2 cửa sổ đón gió tự nhiên'),
(N'P301', N'Phòng 301', N'Đơn', 20.0, 2300000, '/images/p301.jpg', 'TRONG', N'Phòng tầng 3 sạch sẽ, mới sơn sửa'),
(N'P302', N'Phòng 302', N'Đôi', 25.0, 2900000, '/images/p302.jpg', 'DA_THUE', N'Phòng tầng 3 view thoáng đãng'),
(N'P303', N'Phòng 303', N'Studio', 35.0, 4200000, '/images/p303.jpg', 'DA_THUE', N'Căn hộ Studio tầng 3 đầy đủ nội thất gỗ'),
(N'P304', N'Phòng 304', N'Gác lửng', 23.0, 2600000, '/images/p304.jpg', 'DA_DAT', N'Phòng tầng 3 gác cao không đụng đầu'),
(N'P305', N'Phòng 305', N'Đơn', 19.0, 2100000, '/images/p305.jpg', 'BAO_TRI', N'Đang bảo trì chống thấm tường và sơn lại'),
(N'P401', N'Phòng 401', N'Đơn', 22.0, 2400000, '/images/p401.jpg', 'TRONG', N'Phòng tầng 4 có sân phơi chung rộng'),
(N'P402', N'Phòng 402', N'Đôi', 28.0, 3300000, '/images/p402.jpg', 'DA_THUE', N'Phòng tầng 4 hai cửa sổ lớn'),
(N'P403', N'Phòng 403', N'Studio', 35.0, 4500000, '/images/p403.jpg', 'DA_THUE', N'Penthouse Studio tầng 4 view toàn thành phố'),
(N'P404', N'Phòng 404', N'Gác lửng', 25.0, 2800000, '/images/p404.jpg', 'DA_THUE', N'Phòng tầng 4 thoáng mát'),
(N'P405', N'Phòng 405', N'Đơn', 18.0, 2000000, '/images/p405.jpg', 'BAO_TRI', N'Đang thay hệ thống đường ống nước');
GO

-- 3.2. Dữ liệu mẫu bảng khach_thue (20 người với CCCD 12 số duy nhất)
INSERT INTO khach_thue (ho_ten, cccd, so_dien_thoai, email, ngay_sinh, que_quan, gioi_tinh, bien_so_xe, trang_thai) VALUES
(N'Nguyễn Văn An', '079204000001', '0901234501', 'an.nguyen@gmail.com', '2001-03-15', N'Hà Nội', N'Nam', '29A-123.45', 'DANG_THUE'),
(N'Trần Thị Bích', '079204000002', '0901234502', 'bich.tran@gmail.com', '2002-07-20', N'Đà Nẵng', N'Nữ', '43B-234.56', 'DANG_THUE'),
(N'Lê Hoàng Cường', '079204000003', '0901234503', 'cuong.le@gmail.com', '1999-11-05', N'TP. Hồ Chí Minh', N'Nam', '59C-345.67', 'DANG_THUE'),
(N'Phạm Thu Dung', '079204000004', '0901234504', 'dung.pham@gmail.com', '2000-01-25', N'Nam Định', N'Nữ', '18D-456.78', 'DANG_THUE'),
(N'Vũ Minh Đức', '079204000005', '0901234505', 'duc.vu@gmail.com', '2003-05-12', N'Nghệ An', N'Nam', '37E-567.89', 'DANG_THUE'),
(N'Đỗ Thị Hoa', '079204000006', '0901234506', 'hoa.do@gmail.com', '2001-09-18', N'Hải Phòng', N'Nữ', '15F-678.90', 'DANG_THUE'),
(N'Hoàng Văn Hùng', '079204000007', '0901234507', 'hung.hoang@gmail.com', '1998-12-30', N'Bình Định', N'Nam', '77G-789.01', 'DANG_THUE'),
(N'Bùi Thị Mai', '079204000008', '0901234508', 'mai.bui@gmail.com', '2002-04-10', N'Cần Thơ', N'Nữ', '65H-890.12', 'DANG_THUE'),
(N'Ngô Quốc Nam', '079204000009', '0901234509', 'nam.ngo@gmail.com', '2000-08-22', N'Quảng Nam', N'Nam', '92K-901.23', 'DANG_THUE'),
(N'Dương Hải Phong', '079204000010', '0901234510', 'phong.duong@gmail.com', '1997-06-14', N'Thanh Hóa', N'Nam', '36L-012.34', 'DANG_THUE'),
(N'Đinh Ngọc Quỳnh', '079204000011', '0901234511', 'quynh.dinh@gmail.com', '2001-10-08', N'Thái Bình', N'Nữ', '17M-123.45', 'DANG_THUE'),
(N'Mai Văn Sang', '079204000012', '0901234512', 'sang.mai@gmail.com', '1999-02-17', N'Quảng Ngãi', N'Nam', '76N-234.56', 'DANG_THUE'),
(N'Lý Thanh Thảo', '079204000013', '0901234513', 'thao.ly@gmail.com', '2003-12-01', N'Lâm Đồng', N'Nữ', '49P-345.67', 'DANG_THUE'),
(N'Trịnh Minh Tuấn', '079204000014', '0901234514', 'tuan.trinh@gmail.com', '2000-03-29', N'Đắk Lắk', N'Nam', '47R-456.78', 'DANG_THUE'),
(N'Đoàn Bảo Uyên', '079204000015', '0901234515', 'uyen.doan@gmail.com', '2002-08-16', N'Khánh Hòa', N'Nữ', '79S-567.89', 'DANG_THUE'),
(N'Võ Thành Vinh', '079204000016', '0901234516', 'vinh.vo@gmail.com', '1998-05-24', N'Bình Dương', N'Nam', '61T-678.90', 'DANG_THUE'),
(N'Hà Kim Yến', '079204000017', '0901234517', 'yen.ha@gmail.com', '2001-11-11', N'Đồng Nai', N'Nữ', '60U-789.01', 'DANG_THUE'),
(N'Lưu Trọng Khang', '079204000018', '0901234518', 'khang.luu@gmail.com', '1996-09-03', N'An Giang', N'Nam', '67V-890.12', 'DA_TRA_PHONG'),
(N'Tạ Thùy Linh', '079204000019', '0901234519', 'linh.ta@gmail.com', '2002-01-19', N'Tiền Giang', N'Nữ', '63X-901.23', 'DA_TRA_PHONG'),
(N'Phan Công Sơn', '079204000020', '0901234520', 'son.phan@gmail.com', '1995-04-07', N'Bến Tre', N'Nam', '71Y-012.34', 'DA_TRA_PHONG');
GO

-- 3.3. Dữ liệu mẫu bảng tai_khoan (20 tài khoản: 1 ADMIN, 2 STAFF, 17 TENANT)
-- Mật khẩu mẫu định dạng salt:hash
-- Admin: Admin@123 | Staff: Staff@123 | Tenant: Tenant@123
INSERT INTO tai_khoan (ten_dang_nhap, mat_khau, vai_tro, khach_id, trang_thai) VALUES
('admin', 'a1b2c3d4e5f60718:9625a6c207ba950ac83bbd856c466408c021a28e296a970ece60f1fe2244742e', 'ADMIN', NULL, 1),
('staff1', 'f1e2d3c4b5a60719:fa9c403b3a3c50e8e5bbfed4c8615e41f51dda576eb220293473fa8490dc5a54', 'STAFF', NULL, 1),
('staff2', 'c1b2a3f4e5d60720:0e463ee18bee1306a279c8cdb470cd39046078a142ff4b0bb5b35bd847414f80', 'STAFF', NULL, 1),
('tenant01', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 1, 1),
('tenant02', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 2, 1),
('tenant03', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 3, 1),
('tenant04', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 4, 1),
('tenant05', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 5, 1),
('tenant06', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 6, 1),
('tenant07', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 7, 1),
('tenant08', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 8, 1),
('tenant09', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 9, 1),
('tenant10', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 10, 1),
('tenant11', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 11, 1),
('tenant12', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 12, 1),
('tenant13', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 13, 1),
('tenant14', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 14, 1),
('tenant15', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 15, 1),
('tenant16', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 16, 1),
('tenant17', '8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924', 'TENANT', 17, 1);
GO

-- 3.4. Dữ liệu mẫu bảng dat_cho (20 bản ghi: 3 DANG_GIU, 6 HET_HAN, 11 DA_CHUYEN_HOP_DONG)
-- Phòng P104 (id: 4), P204 (id: 9), P304 (id: 14) có trạng thái DANG_GIU với han_giu_cho còn hạn 7 ngày
INSERT INTO dat_cho (phong_id, khach_id, ngay_dat, han_giu_cho, trang_thai, ghi_chu) VALUES
(4, 4, GETDATE(), DATEADD(day, 7, GETDATE()), 'DANG_GIU', N'Khách đặt giữ chỗ P104 qua Web'),
(9, 9, GETDATE(), DATEADD(day, 7, GETDATE()), 'DANG_GIU', N'Khách đặt giữ chỗ P204 qua Socket Client'),
(14, 14, GETDATE(), DATEADD(day, 7, GETDATE()), 'DANG_GIU', N'Khách hẹn ngày mai đến ký hợp đồng'),
(1, 18, '2025-01-10', '2025-01-12', 'HET_HAN', N'Quá hạn giữ chỗ phòng P101, đã giải phóng tự động'),
(6, 19, '2025-02-15', '2025-02-17', 'HET_HAN', N'Quá hạn giữ chỗ phòng P201, đã giải phóng tự động'),
(11, 20, '2025-03-20', '2025-03-22', 'HET_HAN', N'Quá hạn giữ chỗ phòng P301'),
(16, 18, '2025-04-05', '2025-04-07', 'HET_HAN', N'Khách hủy đặt chỗ phòng P401'),
(1, 19, '2025-05-10', '2025-05-12', 'HET_HAN', N'Khách đổi lịch chưa kịp đến xem phòng P101'),
(11, 18, '2025-06-18', '2025-06-20', 'HET_HAN', N'Quá hạn giữ chỗ 48 giờ phòng P301, hệ thống tự thu hồi'),
(2, 2, '2025-12-28', '2025-12-30', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P102'),
(3, 3, '2026-01-28', '2026-01-30', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P103'),
(5, 5, '2026-02-25', '2026-02-27', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P105'),
(7, 7, '2026-01-10', '2026-01-12', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P202'),
(8, 8, '2026-02-10', '2026-02-12', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P203'),
(10, 10, '2026-03-10', '2026-03-12', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P205'),
(12, 12, '2025-12-27', '2025-12-29', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P302'),
(13, 13, '2026-01-27', '2026-01-29', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P303'),
(15, 15, '2025-12-26', '2025-12-28', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P402'),
(16, 16, '2026-01-26', '2026-01-28', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P403'),
(17, 17, '2026-02-24', '2026-02-26', 'DA_CHUYEN_HOP_DONG', N'Đã chuyển thành hợp đồng HD-P404');
GO

-- 3.5. Dữ liệu mẫu bảng hop_dong (20 hợp đồng)
-- 11 hợp đồng HIEU_LUC gắn với 11 phòng DA_THUE (P102, P103, P105, P202, P203, P205, P302, P303, P402, P403, P404)
-- 5 hợp đồng HET_HAN, 4 hợp đồng DA_THANH_LY
INSERT INTO hop_dong (ma_hop_dong, phong_id, khach_id, ngay_bat_dau, ngay_ket_thuc, tien_dat_coc, gia_thue_thoa_thuan, chu_ky_thanh_toan, trang_thai) VALUES
-- 11 Hợp đồng hiệu lực
('HD-P102', 2, 2, '2026-01-01', '2027-01-01', 2800000, 2800000, 1, 'HIEU_LUC'),
('HD-P103', 3, 3, '2026-02-01', '2027-02-01', 3500000, 3500000, 1, 'HIEU_LUC'),
('HD-P105', 5, 5, '2026-03-01', '2027-03-01', 3200000, 3200000, 1, 'HIEU_LUC'),
('HD-P202', 7, 7, '2026-01-15', '2027-01-15', 3000000, 3000000, 1, 'HIEU_LUC'),
('HD-P203', 8, 8, '2026-02-15', '2027-02-15', 3800000, 3800000, 1, 'HIEU_LUC'),
('HD-P205', 10, 10, '2026-03-15', '2027-03-15', 3100000, 3100000, 1, 'HIEU_LUC'),
('HD-P302', 12, 12, '2026-01-01', '2027-01-01', 2900000, 2900000, 1, 'HIEU_LUC'),
('HD-P303', 13, 13, '2026-02-01', '2027-02-01', 4200000, 4200000, 1, 'HIEU_LUC'),
('HD-P402', 15, 15, '2026-01-01', '2027-01-01', 3300000, 3300000, 1, 'HIEU_LUC'),
('HD-P403', 16, 16, '2026-02-01', '2027-02-01', 4500000, 4500000, 1, 'HIEU_LUC'),
('HD-P404', 17, 17, '2026-03-01', '2027-03-01', 2800000, 2800000, 1, 'HIEU_LUC'),
-- 5 Hợp đồng hết hạn (lịch sử thuê cũ)
('HD-OLD-01', 1, 18, '2024-01-01', '2024-12-31', 2000000, 2000000, 1, 'HET_HAN'),
('HD-OLD-02', 6, 19, '2024-02-01', '2025-01-31', 2200000, 2200000, 1, 'HET_HAN'),
('HD-OLD-03', 11, 20, '2024-03-01', '2025-02-28', 2100000, 2100000, 1, 'HET_HAN'),
('HD-OLD-04', 16, 18, '2024-04-01', '2025-03-31', 2200000, 2200000, 1, 'HET_HAN'),
('HD-OLD-05', 4, 19, '2024-05-01', '2025-04-30', 1900000, 1900000, 1, 'HET_HAN'),
-- 4 Hợp đồng đã thanh lý
('HD-TL-01', 15, 20, '2025-01-01', '2025-06-30', 2100000, 2100000, 1, 'DA_THANH_LY'),
('HD-TL-02', 20, 18, '2025-02-01', '2025-07-31', 2000000, 2000000, 1, 'DA_THANH_LY'),
('HD-TL-03', 2, 1, '2025-03-01', '2025-08-31', 2600000, 2600000, 1, 'DA_THANH_LY'),
('HD-TL-04', 7, 6, '2025-04-01', '2025-09-30', 2800000, 2800000, 1, 'DA_THANH_LY');
GO

-- 3.6. Dữ liệu mẫu bảng hoa_don (20 hóa đơn định kỳ)
-- Thỏa mãn ràng buộc UNIQUE(hop_dong_id, thang_nam)
INSERT INTO hoa_don (ma_hoa_don, hop_dong_id, thang_nam, chi_so_dien_cu, chi_so_dien_moi, don_gia_dien, chi_so_nuoc_cu, chi_so_nuoc_moi, don_gia_nuoc, tien_phong, tien_dien, tien_nuoc, tien_dich_vu, tong_tien, trang_thai, ngay_lap, ngay_thanh_toan) VALUES
-- Tháng 09/2026 (Đã thanh toán)
('HDON-202609-01', 1, '09/2026', 120, 160, 3500, 15, 20, 20000, 2800000, 140000, 100000, 100000, 3140000, 'DA_THANH_TOAN', '2026-09-30', '2026-10-02'),
('HDON-202609-02', 2, '09/2026', 200, 270, 3500, 25, 32, 20000, 3500000, 245000, 140000, 100000, 3985000, 'DA_THANH_TOAN', '2026-09-30', '2026-10-03'),
('HDON-202609-03', 3, '09/2026', 150, 210, 3500, 18, 24, 20000, 3200000, 210000, 120000, 100000, 3630000, 'DA_THANH_TOAN', '2026-09-30', '2026-10-01'),
('HDON-202609-04', 4, '09/2026', 180, 250, 3500, 20, 28, 20000, 3000000, 245000, 160000, 100000, 3505000, 'DA_THANH_TOAN', '2026-09-30', '2026-10-04'),
('HDON-202609-05', 5, '09/2026', 310, 400, 3500, 30, 40, 20000, 3800000, 315000, 200000, 100000, 4415000, 'DA_THANH_TOAN', '2026-09-30', '2026-10-02'),
('HDON-202609-06', 6, '09/2026', 140, 200, 3500, 16, 22, 20000, 3100000, 210000, 120000, 100000, 3530000, 'DA_THANH_TOAN', '2026-09-30', '2026-10-05'),
('HDON-202609-07', 7, '09/2026', 110, 165, 3500, 14, 19, 20000, 2900000, 192500, 100000, 100000, 3292500, 'DA_THANH_TOAN', '2026-09-30', '2026-10-03'),
('HDON-202609-08', 8, '09/2026', 220, 310, 3500, 22, 30, 20000, 4200000, 315000, 160000, 100000, 4775000, 'DA_THANH_TOAN', '2026-09-30', '2026-10-01'),
('HDON-202609-09', 9, '09/2026', 190, 260, 3500, 21, 28, 20000, 3300000, 245000, 140000, 100000, 3785000, 'DA_THANH_TOAN', '2026-09-30', '2026-10-04'),
('HDON-202609-10', 10, '09/2026', 280, 380, 3500, 28, 38, 20000, 4500000, 350000, 200000, 100000, 5150000, 'DA_THANH_TOAN', '2026-09-30', '2026-10-02'),
-- Tháng 10/2026 (Chưa thanh toán)
('HDON-202610-01', 1, '10/2026', 160, 205, 3500, 20, 26, 20000, 2800000, 157500, 120000, 100000, 3177500, 'CHUA_THANH_TOAN', '2026-10-15', NULL),
('HDON-202610-02', 2, '10/2026', 270, 345, 3500, 32, 40, 20000, 3500000, 262500, 160000, 100000, 4022500, 'CHUA_THANH_TOAN', '2026-10-15', NULL),
('HDON-202610-03', 3, '10/2026', 210, 280, 3500, 24, 31, 20000, 3200000, 245000, 140000, 100000, 3685000, 'CHUA_THANH_TOAN', '2026-10-15', NULL),
('HDON-202610-04', 4, '10/2026', 250, 320, 3500, 28, 35, 20000, 3000000, 245000, 140000, 100000, 3485000, 'CHUA_THANH_TOAN', '2026-10-15', NULL),
('HDON-202610-05', 5, '10/2026', 400, 510, 3500, 40, 52, 20000, 3800000, 385000, 240000, 100000, 4525000, 'CHUA_THANH_TOAN', '2026-10-15', NULL),
('HDON-202610-06', 6, '10/2026', 200, 270, 3500, 22, 29, 20000, 3100000, 245000, 140000, 100000, 3585000, 'CHUA_THANH_TOAN', '2026-10-15', NULL),
('HDON-202610-07', 7, '10/2026', 165, 225, 3500, 19, 25, 20000, 2900000, 210000, 120000, 100000, 3330000, 'CHUA_THANH_TOAN', '2026-10-15', NULL),
('HDON-202610-08', 8, '10/2026', 310, 410, 3500, 30, 41, 20000, 4200000, 350000, 220000, 100000, 4870000, 'CHUA_THANH_TOAN', '2026-10-15', NULL),
('HDON-202610-09', 9, '10/2026', 260, 335, 3500, 28, 36, 20000, 3300000, 262500, 160000, 100000, 3822500, 'CHUA_THANH_TOAN', '2026-10-15', NULL),
('HDON-202610-10', 10, '10/2026', 380, 490, 3500, 38, 49, 20000, 4500000, 385000, 220000, 100000, 5205000, 'CHUA_THANH_TOAN', '2026-10-15', NULL);
GO

-- 3.7. Dữ liệu mẫu bảng yeu_cau_sua_chua (20 yêu cầu sửa chữa)
INSERT INTO yeu_cau_sua_chua (phong_id, khach_id, noi_dung, tho_sua_chua, chi_phi, muc_do, trang_thai, ngay_tao, ngay_hoan_thanh) VALUES
(15, 20, N'Tường bị thấm nước mưa nghiêm trọng', N'Nguyễn Văn Thợ 1', 1200000, 'CAP_BACH', 'DANG_SUA', '2026-10-10', NULL),
(20, 18, N'Đường ống nước nhà vệ sinh bị rò rỉ', N'Trần Văn Thợ 2', 450000, 'CAP_BACH', 'CHO_TIEP_NHAN', '2026-10-12', NULL),
(2, 2, N'Bóng đèn tuýp LED phòng khách bị cháy', N'Nguyễn Văn Thợ 1', 80000, 'BINH_THUONG', 'HOAN_THANH', '2026-09-05', '2026-09-06'),
(3, 3, N'Vòi sen tắm bị tắc nước chảy yếu', N'Trần Văn Thợ 2', 150000, 'BINH_THUONG', 'HOAN_THANH', '2026-09-10', '2026-09-11'),
(5, 5, N'Khóa tay gạt cửa phòng bị kẹt', N'Nguyễn Văn Thợ 1', 200000, 'CAP_BACH', 'HOAN_THANH', '2026-09-12', '2026-09-12'),
(7, 7, N'Bồn rửa mặt thoát nước chậm', N'Trần Văn Thợ 2', 100000, 'BINH_THUONG', 'HOAN_THANH', '2026-09-15', '2026-09-16'),
(8, 8, N'Điều hòa chảy nước trong phòng ngủ', N'Lê Văn Thợ Lạnh', 350000, 'BINH_THUONG', 'HOAN_THANH', '2026-09-18', '2026-09-19'),
(10, 10, N'Aptomat tổng bị nhảy khi bật ấm siêu tốc', N'Nguyễn Văn Thợ 1', 120000, 'CAP_BACH', 'HOAN_THANH', '2026-09-20', '2026-09-20'),
(12, 12, N'Bản lề cánh cửa sổ bị rỉ sét', N'Trần Văn Thợ 2', 90000, 'BINH_THUONG', 'HOAN_THANH', '2026-09-22', '2026-09-23'),
(13, 13, N'Tủ lạnh kêu to bất thường', N'Lê Văn Thợ Lạnh', 250000, 'BINH_THUONG', 'HOAN_THANH', '2026-09-25', '2026-09-26'),
(15, 15, N'Ổ cắm điện khu vực bếp bị chập tia lửa', N'Nguyễn Văn Thợ 1', 150000, 'CAP_BACH', 'HOAN_THANH', '2026-09-28', '2026-09-28'),
(16, 16, N'Mùi hôi thoát sàn nhà tắm', N'Trần Văn Thợ 2', 180000, 'BINH_THUONG', 'HOAN_THANH', '2026-10-01', '2026-10-02'),
(17, 17, N'Dây cấp nước bồn cầu bị rạn nứt', N'Nguyễn Văn Thợ 1', 110000, 'CAP_BACH', 'HOAN_THANH', '2026-10-03', '2026-10-03'),
(2, 2, N'Bản lề tủ bếp trên bị lỏng ốc vít', N'Trần Văn Thợ 2', 50000, 'BINH_THUONG', 'CHO_TIEP_NHAN', '2026-10-16', NULL),
(3, 3, N'Công tắc bình nóng lạnh chập chờn', N'Nguyễn Văn Thợ 1', 130000, 'CAP_BACH', 'DANG_SUA', '2026-10-15', NULL),
(7, 7, N'Quạt hút gió toilet không quay', N'Nguyễn Văn Thợ 1', 220000, 'BINH_THUONG', 'CHO_TIEP_NHAN', '2026-10-17', NULL),
(8, 8, N'Lưới chống muỗi cửa sổ bị rách', N'Trần Văn Thợ 2', 150000, 'BINH_THUONG', 'CHO_TIEP_NHAN', '2026-10-17', NULL),
(10, 10, N'Gương phòng tắm bị nứt mép', N'Trần Văn Thợ 2', 300000, 'BINH_THUONG', 'CHO_TIEP_NHAN', '2026-10-18', NULL),
(12, 12, N'Đèn ban công ngoài trời bị hỏng', N'Nguyễn Văn Thợ 1', 70000, 'BINH_THUONG', 'CHO_TIEP_NHAN', '2026-10-18', NULL),
(13, 13, N'Phao bồn cầu không ngắt nước chảy liên tục', N'Trần Văn Thợ 2', 160000, 'CAP_BACH', 'DANG_SUA', '2026-10-16', NULL);
GO
