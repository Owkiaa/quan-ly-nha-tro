package com.nhatro.service.impl;

import com.nhatro.entity.KhachThue;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.ResourceNotFoundException;
import com.nhatro.repository.DatChoRepository;
import com.nhatro.repository.HopDongRepository;
import com.nhatro.repository.KhachThueRepository;
import com.nhatro.repository.TaiKhoanRepository;
import com.nhatro.service.KhachThueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class KhachThueServiceImpl implements KhachThueService {

    private final KhachThueRepository khachThueRepository;
    private final HopDongRepository hopDongRepository;
    private final DatChoRepository datChoRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public KhachThueServiceImpl(KhachThueRepository khachThueRepository,
                                 HopDongRepository hopDongRepository,
                                 DatChoRepository datChoRepository,
                                 TaiKhoanRepository taiKhoanRepository) {
        this.khachThueRepository = khachThueRepository;
        this.hopDongRepository = hopDongRepository;
        this.datChoRepository = datChoRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KhachThue> search(String keyword, String trangThai, Pageable pageable) {
        return khachThueRepository.search(keyword, trangThai, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhachThue> getAll() {
        return khachThueRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public KhachThue getById(Integer id) {
        return khachThueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách thuê có mã số: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public KhachThue getByCccd(String cccd) {
        return khachThueRepository.findByCccd(cccd)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách thuê có CCCD: " + cccd));
    }

    @Override
    public KhachThue create(KhachThue khachThue) {
        validateKhachThue(khachThue);

        String cccd = khachThue.getCccd().trim();
        if (khachThueRepository.existsByCccd(cccd)) {
            throw new BusinessException("Số CCCD '" + cccd + "' đã được sử dụng trong hệ thống");
        }
        khachThue.setCccd(cccd);

        if (khachThue.getTrangThai() == null || khachThue.getTrangThai().trim().isEmpty()) {
            khachThue.setTrangThai("DANG_THUE");
        }

        return khachThueRepository.save(khachThue);
    }

    @Override
    public KhachThue update(Integer id, KhachThue khachThue) {
        KhachThue existing = getById(id);
        validateKhachThue(khachThue);

        String cccd = khachThue.getCccd().trim();
        if (!existing.getCccd().equals(cccd) && khachThueRepository.existsByCccd(cccd)) {
            throw new BusinessException("Số CCCD '" + cccd + "' đã được sử dụng trong hệ thống");
        }

        existing.setHoTen(khachThue.getHoTen().trim());
        existing.setCccd(cccd);
        existing.setSoDienThoai(khachThue.getSoDienThoai().trim());
        existing.setEmail(khachThue.getEmail());
        existing.setNgaySinh(khachThue.getNgaySinh());
        existing.setQueQuan(khachThue.getQueQuan());
        existing.setGioiTinh(khachThue.getGioiTinh());
        existing.setBienSoXe(khachThue.getBienSoXe());
        existing.setTrangThai(khachThue.getTrangThai());

        return khachThueRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        KhachThue khach = getById(id);

        boolean hasContract = hopDongRepository.existsByKhachThueId(id);
        boolean hasBooking = datChoRepository.existsByKhachThueIdAndTrangThai(id, "DANG_GIU");
        boolean hasAccount = taiKhoanRepository.existsByKhachThueId(id);

        if (hasContract || hasBooking || hasAccount) {
            throw new BusinessException("Không thể xóa khách thuê do đang có hợp đồng hoặc đang đặt giữ chỗ phòng.");
        }

        khachThueRepository.delete(khach);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTrangThai(String trangThai) {
        return khachThueRepository.countByTrangThai(trangThai);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return khachThueRepository.count();
    }

    private void validateKhachThue(KhachThue khachThue) {
        if (khachThue.getHoTen() == null || khachThue.getHoTen().trim().isEmpty()) {
            throw new BusinessException("Họ và tên khách thuê không được để trống");
        }
        if (khachThue.getCccd() == null || khachThue.getCccd().trim().isEmpty()) {
            throw new BusinessException("Số CCCD/CMND không được để trống");
        }
        if (khachThue.getSoDienThoai() == null || khachThue.getSoDienThoai().trim().isEmpty()) {
            throw new BusinessException("Số điện thoại không được để trống");
        }
    }
}
