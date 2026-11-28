package com.nhatro.service.impl;

import com.nhatro.entity.YeuCauSuaChua;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu sửa chữa ID: " + id));
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
        if ("HOAN_THANH".equals(yeuCau.getTrangThai())) {
            throw new BusinessException("Không thể phân công lại phiếu sửa chữa đã hoàn thành.");
        }
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
        if ("HOAN_THANH".equals(yeuCau.getTrangThai())) {
            throw new BusinessException("Phiếu sửa chữa đã hoàn thành, không thể nghiệm thu lại.");
        }
        yeuCau.setChiPhi(chiPhi);
        yeuCau.setTrangThai("HOAN_THANH");
        yeuCau.setNgayHoanThanh(LocalDateTime.now());
        return yeuCauRepository.save(yeuCau);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        YeuCauSuaChua yeuCau = getById(id);
        yeuCauRepository.delete(yeuCau);
    }

    @Override
    public long countByTrangThai(String trangThai) {
        return yeuCauRepository.countByTrangThai(trangThai);
    }

    @Override
    public long countByMucDo(String mucDo) {
        return yeuCauRepository.countByMucDo(mucDo);
    }

    @Override
    public BigDecimal sumTongChiPhi() {
        BigDecimal total = yeuCauRepository.sumTongChiPhi();
        return total != null ? total : BigDecimal.ZERO;
    }
}