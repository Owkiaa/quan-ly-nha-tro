package com.nhatro.service.impl;

import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.ResourceNotFoundException;
import com.nhatro.repository.DatChoRepository;
import com.nhatro.repository.HopDongRepository;
import com.nhatro.repository.PhongTroRepository;
import com.nhatro.repository.YeuCauSuaChuaRepository;
import com.nhatro.service.PhongTroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PhongTroServiceImpl implements PhongTroService {

    private final PhongTroRepository phongTroRepository;
    private final HopDongRepository hopDongRepository;
    private final DatChoRepository datChoRepository;
    private final YeuCauSuaChuaRepository yeuCauSuaChuaRepository;

    public PhongTroServiceImpl(PhongTroRepository phongTroRepository,
                               HopDongRepository hopDongRepository,
                               DatChoRepository datChoRepository,
                               YeuCauSuaChuaRepository yeuCauSuaChuaRepository) {
        this.phongTroRepository = phongTroRepository;
        this.hopDongRepository = hopDongRepository;
        this.datChoRepository = datChoRepository;
        this.yeuCauSuaChuaRepository = yeuCauSuaChuaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhongTro> search(String keyword, String trangThai, Pageable pageable) {
        return phongTroRepository.search(keyword, trangThai, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhongTro> getAll() {
        return phongTroRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PhongTro getById(Integer id) {
        return phongTroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng trọ có mã số: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PhongTro getByMaPhong(String maPhong) {
        return phongTroRepository.findByMaPhong(maPhong)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng trọ có mã: " + maPhong));
    }

    @Override
    public PhongTro create(PhongTro phongTro, MultipartFile imageFile) {
        validatePhongTro(phongTro);

        String maPhong = phongTro.getMaPhong().trim().toUpperCase();
        if (phongTroRepository.existsByMaPhong(maPhong)) {
            throw new BusinessException("Mã phòng '" + maPhong + "' đã tồn tại trong hệ thống");
        }
        phongTro.setMaPhong(maPhong);

        if (phongTro.getTrangThai() == null || phongTro.getTrangThai().trim().isEmpty()) {
            phongTro.setTrangThai("TRONG");
        } else {
            String initStatus = phongTro.getTrangThai().trim().toUpperCase();
            if (!"TRONG".equals(initStatus) && !"BAO_TRI".equals(initStatus)) {
                throw new BusinessException("Phòng mới tạo chỉ được phép ở trạng thái TRONG hoặc BAO_TRI");
            }
            phongTro.setTrangThai(initStatus);
        }

        String savedImagePath = handleUploadImage(imageFile);
        if (savedImagePath != null) {
            phongTro.setHinhAnh(savedImagePath);
        } else if (phongTro.getHinhAnh() == null || phongTro.getHinhAnh().trim().isEmpty()) {
            phongTro.setHinhAnh("/images/default-room.jpg");
        }

        return phongTroRepository.save(phongTro);
    }

    @Override
    public PhongTro update(Integer id, PhongTro phongTro, MultipartFile imageFile) {
        PhongTro existing = getById(id);
        validatePhongTro(phongTro);

        String maPhong = phongTro.getMaPhong().trim().toUpperCase();
        if (!existing.getMaPhong().equalsIgnoreCase(maPhong) && phongTroRepository.existsByMaPhong(maPhong)) {
            throw new BusinessException("Mã phòng '" + maPhong + "' đã tồn tại trong hệ thống");
        }

        String requestedStatus = (phongTro.getTrangThai() != null && !phongTro.getTrangThai().trim().isEmpty())
                ? phongTro.getTrangThai().trim().toUpperCase()
                : existing.getTrangThai();

        if (!requestedStatus.equals(existing.getTrangThai())) {
            if ("DA_THUE".equals(existing.getTrangThai())
                    || hopDongRepository.existsByPhongTroIdAndTrangThai(id, "HIEU_LUC")) {
                throw new BusinessException("Không thể thay đổi thủ công trạng thái phòng đang thuê (DA_THUE). Vui lòng thanh lý hợp đồng trước.");
            }
            if ("DA_DAT".equals(existing.getTrangThai())
                    || datChoRepository.existsByPhongTroIdAndTrangThai(id, "DANG_GIU")) {
                throw new BusinessException("Không thể thay đổi thủ công trạng thái phòng đang giữ chỗ (DA_DAT). Vui lòng xử lý giữ chỗ trước.");
            }
            if (!"TRONG".equals(requestedStatus) && !"BAO_TRI".equals(requestedStatus)) {
                throw new BusinessException("Không thể gán thủ công trạng thái Đã thuê / Đã đặt. Vui lòng thực hiện qua luồng Đặt chỗ hoặc Hợp đồng.");
            }
        }

        existing.setMaPhong(maPhong);
        existing.setTenPhong(phongTro.getTenPhong().trim());
        existing.setLoaiPhong(phongTro.getLoaiPhong());
        existing.setDienTich(phongTro.getDienTich());
        existing.setGiaThue(phongTro.getGiaThue());
        existing.setTrangThai(requestedStatus);
        existing.setMoTa(phongTro.getMoTa());

        String savedImagePath = handleUploadImage(imageFile);
        if (savedImagePath != null) {
            existing.setHinhAnh(savedImagePath);
        } else if (phongTro.getHinhAnh() != null && !phongTro.getHinhAnh().trim().isEmpty()) {
            existing.setHinhAnh(phongTro.getHinhAnh().trim());
        }

        return phongTroRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        PhongTro phong = getById(id);

        boolean hasContract = hopDongRepository.existsByPhongTroId(id);
        boolean hasBooking = datChoRepository.existsByPhongTroIdAndTrangThai(id, "DANG_GIU");
        boolean hasUnresolvedIssue = yeuCauSuaChuaRepository.existsByPhongTroIdAndTrangThaiNot(id, "HOAN_THANH");

        if (hasContract || hasBooking || hasUnresolvedIssue) {
            throw new BusinessException("Không thể xóa phòng " + phong.getMaPhong() +
                    " do đang có hợp đồng, yêu cầu sửa chữa hoặc đang có khách đặt giữ chỗ. Vui lòng kiểm tra lại.");
        }

        phongTroRepository.delete(phong);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTrangThai(String trangThai) {
        return phongTroRepository.countByTrangThai(trangThai);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return phongTroRepository.count();
    }

    private void validatePhongTro(PhongTro phongTro) {
        if (phongTro.getMaPhong() == null || phongTro.getMaPhong().trim().isEmpty()) {
            throw new BusinessException("Mã phòng không được để trống");
        }
        if (phongTro.getTenPhong() == null || phongTro.getTenPhong().trim().isEmpty()) {
            throw new BusinessException("Tên phòng không được để trống");
        }
        if (phongTro.getGiaThue() == null || phongTro.getGiaThue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Giá thuê phòng phải lớn hơn 0");
        }
        if (phongTro.getDienTich() != null && phongTro.getDienTich() <= 0) {
            throw new BusinessException("Diện tích phòng phải lớn hơn 0");
        }
    }

    private String handleUploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path uploadDir = Paths.get("uploads", "rooms");
            File dir = uploadDir.toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String cleanName = (originalName != null) ? originalName.replaceAll("[^a-zA-Z0-9._-]", "") : "room.jpg";
            String newFilename = UUID.randomUUID().toString().substring(0, 8) + "_" + cleanName;
            Path targetPath = uploadDir.resolve(newFilename);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/rooms/" + newFilename;
        } catch (IOException e) {
            throw new BusinessException("Lỗi lưu file ảnh phòng trọ: " + e.getMessage());
        }
    }
}
