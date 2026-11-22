package com.nhatro.service.impl;

import com.nhatro.entity.YeuCauSuaChua;
import com.nhatro.exception.BusinessException;
import com.nhatro.repository.YeuCauSuaChuaRepository;
import com.nhatro.service.YeuCauSuaChuaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class YeuCauSuaChuaServiceImpl implements YeuCauSuaChuaService {

    @Autowired
    private YeuCauSuaChuaRepository yeuCauRepository;

    @Override
    public Page<YeuCauSuaChua> search(String keyword, String trangThai, Pageable pageable) {
        return yeuCauRepository.search(keyword, trangThai, pageable);
    }

    @Override
    public YeuCauSuaChua getById(Integer id) {
        return yeuCauRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu sửa chữa ID: " + id));
    }

    @Override
    @Transactional
    public YeuCauSuaChua create(YeuCauSuaChua yeuCauSuaChua) {
        yeuCauSuaChua.setTrangThai("CHO_TIEP_NHAN");
        yeuCauSuaChua.setNgayTao(LocalDateTime.now());
        yeuCauSuaChua.setChiPhi(BigDecimal.ZERO);
        return yeuCauRepository.save(yeuCauSuaChua);
    }

    @Override
    @Transactional
    public YeuCauSuaChua phanCongTho(Integer id, String thoSuaChua) {
        if (thoSuaChua == null || thoSuaChua.trim().isEmpty()) {
            throw new BusinessException("Tên thợ sửa chữa không được để trống.");
        }
        YeuCauSuaChua yeuCau = getById(id);
        yeuCau.setThoSuaChua(thoSuaChua.trim());
        yeuCau.setTrangThai("DANG_SUA");
        return yeuCauRepository.save(yeuCau);
    }

    @Override
    @Transactional
    public YeuCauSuaChua capNhatChiPhiVaHoanThanh(Integer id, BigDecimal chiPhi) {
        if (chiPhi == null || chiPhi.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Chi phí sửa chữa không được để trống hoặc âm.");
        }
        YeuCauSuaChua yeuCau = getById(id);
        yeuCau.setChiPhi(chiPhi);
        yeuCau.setTrangThai("HOAN_THANH");
        yeuCau.setNgayHoanThanh(LocalDateTime.now());
        return yeuCauRepository.save(yeuCau);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        yeuCauRepository.deleteById(id);
    }
}