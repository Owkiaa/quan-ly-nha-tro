package com.nhatro.service.impl;

import com.nhatro.entity.TaiKhoan;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.ResourceNotFoundException;
import com.nhatro.repository.TaiKhoanRepository;
import com.nhatro.service.TaiKhoanService;
import com.nhatro.util.PasswordUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class TaiKhoanServiceImpl implements TaiKhoanService {

    private final TaiKhoanRepository taiKhoanRepository;

    public TaiKhoanServiceImpl(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TaiKhoan login(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty() || matKhau == null || matKhau.trim().isEmpty()) {
            throw new BusinessException("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
        }

        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(tenDangNhap.trim())
                .orElseThrow(() -> new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        if (Boolean.FALSE.equals(taiKhoan.getTrangThai())) {
            throw new BusinessException("Tài khoản đang bị khóa. Vui lòng liên hệ quản trị viên");
        }

        if (!PasswordUtil.verify(matKhau, taiKhoan.getMatKhau())) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        return taiKhoan;
    }

    @Override
    @Transactional(readOnly = true)
    public TaiKhoan getById(Integer id) {
        return taiKhoanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản có mã: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public TaiKhoan getByTenDangNhap(String tenDangNhap) {
        return taiKhoanRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + tenDangNhap));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaiKhoan> search(String keyword, String vaiTro, Pageable pageable) {
        return taiKhoanRepository.search(keyword, vaiTro, pageable);
    }

    @Override
    public TaiKhoan create(TaiKhoan taiKhoan, String rawPassword) {
        if (taiKhoan.getTenDangNhap() == null || taiKhoan.getTenDangNhap().trim().isEmpty()) {
            throw new BusinessException("Tên đăng nhập không được để trống");
        }
        if (rawPassword == null || rawPassword.trim().length() < 6) {
            throw new BusinessException("Mật khẩu phải có độ dài tối thiểu 6 ký tự");
        }

        String username = taiKhoan.getTenDangNhap().trim();
        if (taiKhoanRepository.existsByTenDangNhap(username)) {
            throw new BusinessException("Tên đăng nhập '" + username + "' đã được sử dụng");
        }

        taiKhoan.setTenDangNhap(username);
        taiKhoan.setMatKhau(PasswordUtil.hash(rawPassword));
        if (taiKhoan.getTrangThai() == null) {
            taiKhoan.setTrangThai(true);
        }
        if (taiKhoan.getNgayTao() == null) {
            taiKhoan.setNgayTao(LocalDateTime.now());
        }

        return taiKhoanRepository.save(taiKhoan);
    }

    @Override
    public void changePassword(Integer id, String oldPassword, String newPassword, String confirmPassword) {
        TaiKhoan taiKhoan = getById(id);

        if (!PasswordUtil.verify(oldPassword, taiKhoan.getMatKhau())) {
            throw new BusinessException("Mật khẩu hiện tại không chính xác");
        }

        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new BusinessException("Mật khẩu mới phải có tối thiểu 6 ký tự");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("Xác nhận mật khẩu mới không trùng khớp");
        }

        taiKhoan.setMatKhau(PasswordUtil.hash(newPassword));
        taiKhoanRepository.save(taiKhoan);
    }

    @Override
    public void toggleStatus(Integer id, Integer currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BusinessException("Bạn không thể tự khóa tài khoản của chính mình");
        }
        TaiKhoan taiKhoan = getById(id);
        taiKhoan.setTrangThai(!Boolean.TRUE.equals(taiKhoan.getTrangThai()));
        taiKhoanRepository.save(taiKhoan);
    }

    @Override
    public void resetPassword(Integer id, String newPassword) {
        TaiKhoan taiKhoan = getById(id);
        String pass = (newPassword == null || newPassword.trim().isEmpty()) ? "123456" : newPassword.trim();
        taiKhoan.setMatKhau(PasswordUtil.hash(pass));
        taiKhoanRepository.save(taiKhoan);
    }
}
