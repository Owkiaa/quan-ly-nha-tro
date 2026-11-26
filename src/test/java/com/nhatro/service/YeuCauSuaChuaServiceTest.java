package com.nhatro.service;

import com.nhatro.entity.YeuCauSuaChua;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.ResourceNotFoundException;
import com.nhatro.repository.YeuCauSuaChuaRepository;
import com.nhatro.service.impl.YeuCauSuaChuaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YeuCauSuaChuaServiceTest {

    @Mock
    private YeuCauSuaChuaRepository yeuCauRepository;

    @InjectMocks
    private YeuCauSuaChuaServiceImpl service;

    private YeuCauSuaChua phieuMau;

    @BeforeEach
    void setUp() {
        phieuMau = new YeuCauSuaChua();
        phieuMau.setId(1);
        phieuMau.setNoiDung("Rò rỉ ống nước");
        phieuMau.setMucDo("CAP_BACH");
        phieuMau.setTrangThai("CHO_TIEP_NHAN");
        phieuMau.setChiPhi(BigDecimal.ZERO);
    }

    @Test
    void getById_tonTai_traVePhieu() {
        when(yeuCauRepository.findById(1)).thenReturn(Optional.of(phieuMau));

        YeuCauSuaChua result = service.getById(1);

        assertEquals(1, result.getId());
        assertEquals("CHO_TIEP_NHAN", result.getTrangThai());
        verify(yeuCauRepository).findById(1);
    }

    @Test
    void getById_khongTonTai_nemResourceNotFoundException() {
        when(yeuCauRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(999));
        verify(yeuCauRepository).findById(999);
    }

    @Test
    void create_macDinhChoTiepNhanVaChiPhiKhong() {
        YeuCauSuaChua moi = new YeuCauSuaChua();
        moi.setNoiDung("Hỏng bóng đèn");
        moi.setMucDo("BINH_THUONG");
        when(yeuCauRepository.save(any(YeuCauSuaChua.class))).thenAnswer(inv -> inv.getArgument(0));

        YeuCauSuaChua saved = service.create(moi);

        assertEquals("CHO_TIEP_NHAN", saved.getTrangThai());
        assertEquals(BigDecimal.ZERO, saved.getChiPhi());
        assertNotNull(saved.getNgayTao());
        verify(yeuCauRepository).save(moi);
    }

    @Test
    void phanCongTho_hopLe_chuyenTrangThaiDangSua() {
        when(yeuCauRepository.findById(1)).thenReturn(Optional.of(phieuMau));
        when(yeuCauRepository.save(any(YeuCauSuaChua.class))).thenAnswer(inv -> inv.getArgument(0));

        YeuCauSuaChua result = service.phanCongTho(1, "  Nguyễn Văn Thợ  ");

        assertEquals("Nguyễn Văn Thợ", result.getThoSuaChua());
        assertEquals("DANG_SUA", result.getTrangThai());
        verify(yeuCauRepository).save(phieuMau);
    }

    @Test
    void phanCongTho_daHoanThanh_nemBusinessException() {
        phieuMau.setTrangThai("HOAN_THANH");
        when(yeuCauRepository.findById(1)).thenReturn(Optional.of(phieuMau));

        assertThrows(BusinessException.class, () -> service.phanCongTho(1, "Nguyễn Văn Thợ"));
        verify(yeuCauRepository, never()).save(any());
    }

    @Test
    void capNhatChiPhiVaHoanThanh_hopLe_chuyenTrangThaiHoanThanh() {
        when(yeuCauRepository.findById(1)).thenReturn(Optional.of(phieuMau));
        when(yeuCauRepository.save(any(YeuCauSuaChua.class))).thenAnswer(inv -> inv.getArgument(0));

        YeuCauSuaChua result = service.capNhatChiPhiVaHoanThanh(1, new BigDecimal("150000"));

        assertEquals("HOAN_THANH", result.getTrangThai());
        assertEquals(new BigDecimal("150000"), result.getChiPhi());
        assertNotNull(result.getNgayHoanThanh());
        verify(yeuCauRepository).save(phieuMau);
    }

    @Test
    void capNhatChiPhiVaHoanThanh_daHoanThanh_nemBusinessException() {
        phieuMau.setTrangThai("HOAN_THANH");
        when(yeuCauRepository.findById(1)).thenReturn(Optional.of(phieuMau));

        assertThrows(BusinessException.class, () -> service.capNhatChiPhiVaHoanThanh(1, new BigDecimal("150000")));
        verify(yeuCauRepository, never()).save(any());
    }

    @Test
    void delete_khongTonTai_nemResourceNotFoundException() {
        when(yeuCauRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(404));
        verify(yeuCauRepository, never()).delete(any());
    }

    @Test
    void delete_tonTai_xoaThanhCong() {
        when(yeuCauRepository.findById(1)).thenReturn(Optional.of(phieuMau));

        service.delete(1);

        verify(yeuCauRepository).delete(phieuMau);
    }
}