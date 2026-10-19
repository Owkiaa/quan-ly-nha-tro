-- Script 02: Khung schema bảng và dữ liệu mẫu cho hệ thống quản lí nhà trọ
USE NhaTroDB;
GO

-- (Các thành viên trong nhóm sẽ bổ sung bảng cụ thể: PhongTro, KhachThue, HopDong, HoaDon, DichVu,...)
-- Ví dụ khung bảng kiểm tra kết nối:
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'he_thong')
BEGIN
    CREATE TABLE he_thong (
        id INT IDENTITY(1,1) PRIMARY KEY,
        ten_he_thong NVARCHAR(100) NOT NULL,
        ngay_tao DATETIME DEFAULT GETDATE(),
        ghi_chu NVARCHAR(255)
    );

    INSERT INTO he_thong (ten_he_thong, ghi_chu)
    VALUES (N'Hệ thống Quản lí Nhà trọ', N'Khởi tạo khung đồ án');
END
GO
