-- Script 02: Khung schema bảng và dữ liệu mẫu cho hệ thống quản lí nhà trọ (MySQL)
-- Thực thi trên MySQL Workbench hoặc MySQL CLI sau khi đã chạy script 01

USE nhatro_db;

-- Bảng hệ thống kiểm tra kết nối
CREATE TABLE IF NOT EXISTS he_thong (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ten_he_thong VARCHAR(100) NOT NULL,
    ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ghi_chu VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO he_thong (ten_he_thong, ghi_chu)
VALUES ('Hệ thống Quản lí Nhà trọ', 'Khởi tạo khung đồ án MySQL');
