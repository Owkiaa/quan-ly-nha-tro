-- Script 01: Tạo cơ sở dữ liệu NhaTroDB cho hệ thống quản lí nhà trọ
-- Thực thi trên SQL Server Management Studio (SSMS) hoặc Azure Data Studio

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'NhaTroDB')
BEGIN
    CREATE DATABASE NhaTroDB;
    PRINT N'Đã tạo cơ sở dữ liệu NhaTroDB thành công.';
END
ELSE
BEGIN
    PRINT N'Cơ sở dữ liệu NhaTroDB đã tồn tại.';
END
GO
