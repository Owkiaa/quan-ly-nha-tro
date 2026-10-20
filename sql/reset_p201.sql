-- ==============================================================
-- SCRIPT RESET PHÒNG P201 PHỤC VỤ KIỂM THỬ ĐỒNG THỜI (CONCURRENCY)
-- Đưa phòng P201 về trạng thái TRONG và dọn dẹp các bản ghi đặt chỗ liên quan
-- ==============================================================

USE NhaTroDB;
GO

-- 1. Xóa các bản ghi đặt chỗ của phòng P201 trong bảng dat_cho
DELETE FROM dat_cho
WHERE phong_id IN (SELECT id FROM phong_tro WHERE ma_phong = 'P201');
GO

-- 2. Cập nhật trạng thái phòng P201 về TRONG
UPDATE phong_tro
SET trang_thai = 'TRONG'
WHERE ma_phong = 'P201';
GO

-- 3. Kiểm tra lại thông tin phòng P201
SELECT id, ma_phong, ten_phong, gia_thue, trang_thai
FROM phong_tro
WHERE ma_phong = 'P201';
GO
