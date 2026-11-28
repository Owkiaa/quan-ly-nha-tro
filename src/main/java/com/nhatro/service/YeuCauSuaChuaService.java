package com.nhatro.service;

import com.nhatro.entity.YeuCauSuaChua;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface YeuCauSuaChuaService {

    Page<YeuCauSuaChua> search(String keyword, String trangThai, Pageable pageable);

    YeuCauSuaChua getById(Integer id);

    // Dành cho khách thuê gửi yêu cầu mới
    YeuCauSuaChua create(YeuCauSuaChua yeuCauSuaChua);

    // Chức năng cho Admin/Staff: Phân công thợ sửa chữa
    YeuCauSuaChua phanCongTho(Integer id, String thoSuaChua);

    // Chức năng cho Admin/Staff: Hoàn thành sửa chữa và cập nhật chi phí
    YeuCauSuaChua capNhatChiPhiVaHoanThanh(Integer id, BigDecimal chiPhi);

    void delete(Integer id);
}