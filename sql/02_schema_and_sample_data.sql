-- Script 02: Khung schema bảng và dữ liệu mẫu cho hệ thống quản lí nhà trọ (SQL Server)
-- Thực thi trên SSMS hoặc Azure Data Studio sau khi đã chạy script 01

USE NhaTroDB;
GO

-- Bảng hệ thống kiểm tra kết nối
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'he_thong')
BEGIN
    CREATE TABLE he_thong (
        id INT IDENTITY(1,1) PRIMARY KEY,
        ten_he_thong NVARCHAR(100) NOT NULL,
        ngay_tao DATETIME DEFAULT GETDATE(),
        ghi_chu NVARCHAR(255)
    );

    INSERT INTO he_thong (ten_he_thong, ghi_chu)
    VALUES (N'Hệ thống Quản lí Nhà trọ', N'Khởi tạo khung đồ án SQL Server');
END
GO
