-- Script 01: Tạo cơ sở dữ liệu nhatro_db cho hệ thống quản lí nhà trọ (MySQL)
-- Thực thi trên MySQL Workbench, phpMyAdmin hoặc MySQL CLI

CREATE DATABASE IF NOT EXISTS nhatro_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE nhatro_db;

SELECT 'Đã tạo cơ sở dữ liệu nhatro_db thành công!' AS thong_bao;
