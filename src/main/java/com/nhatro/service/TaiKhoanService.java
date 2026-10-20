package com.nhatro.service;

import com.nhatro.entity.TaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaiKhoanService {

    TaiKhoan login(String tenDangNhap, String matKhau);

    TaiKhoan getById(Integer id);

    TaiKhoan getByTenDangNhap(String tenDangNhap);

    Page<TaiKhoan> search(String keyword, String vaiTro, Pageable pageable);

    TaiKhoan create(TaiKhoan taiKhoan, String rawPassword);

    void changePassword(Integer id, String oldPassword, String newPassword, String confirmPassword);

    void toggleStatus(Integer id, Integer currentUserId);

    void resetPassword(Integer id, String newPassword);
}
