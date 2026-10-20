-- ==============================================================
-- SCRIPT 01: TẠO CƠ SỞ DỮ LIỆU NHATRODB (MICROSOFT SQL SERVER)
-- ==============================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'NhaTroDB')
BEGIN
    CREATE DATABASE NhaTroDB
    COLLATE Vietnamese_CI_AS;
END
GO
