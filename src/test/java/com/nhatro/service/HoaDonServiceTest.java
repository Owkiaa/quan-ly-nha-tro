package com.nhatro.service;

import com.nhatro.dto.HoaDonCreateDTO;
import com.nhatro.entity.HoaDon;
import com.nhatro.entity.HopDong;
import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.repository.HoaDonRepository;
import com.nhatro.repository.HopDongRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoaDonServiceTest {

    @Mock
    private HoaDonRepository hoaDonRepository;

    @Mock
    private HopDongRepository hopDongRepository;

    @InjectMocks
    private HoaDonService hoaDonService;

    private HopDong hopDongHieuLuc;

    @BeforeEach
    void setUp() {
        PhongTro phong = new PhongTro();
        phong.setId(1);
        phong.setMaPhong("P102");
        phong.setTenPhong("Phòng 102");

        hopDongHieuLuc = new HopDong();
        hopDongHieuLuc.setId(10);
        hopDongHieuLuc.setMaHopDong("HD-P102");
        hopDongHieuLuc.setPhongTro(phong);
        hopDongHieuLuc.setGiaThueThoaThuan(new BigDecimal("2800000"));
        hopDongHieuLuc.setTrangThai("HIEU_LUC");
    }

    private HoaDonCreateDTO validDto() {
        HoaDonCreateDTO dto = new HoaDonCreateDTO();
        dto.setHopDongId(10);
        dto.setThangNam("11/2026");
        dto.setChiSoDienCu(100);
        dto.setChiSoDienMoi(150); // 50 kWh * 3500 = 175,000
        dto.setDonGiaDien(new BigDecimal("3500"));
        dto.setChiSoNuocCu(20);
        dto.setChiSoNuocMoi(25);  // 5 m3 * 20000 = 100,000
        dto.setDonGiaNuoc(new BigDecimal("20000"));
        dto.setTienPhong(new BigDecimal("2800000"));
        dto.setTienDichVu(new BigDecimal("100000"));
        return dto;
    }

    @Test
    void taoHoaDon_hopLe_tinhDungTienDienNuocDichVuVaTongTien() {
        when(hopDongRepository.findById(10)).thenReturn(Optional.of(hopDongHieuLuc));
        when(hoaDonRepository.existsByHopDongIdAndThangNam(10, "11/2026")).thenReturn(false);
        when(hoaDonRepository.existsByMaHoaDon(anyString())).thenReturn(false);
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(inv -> inv.getArgument(0));

        HoaDon saved = hoaDonService.taoHoaDon(validDto());

        assertEquals("HD-112026-P102", saved.getMaHoaDon());
        assertEquals(new BigDecimal("175000"), saved.getTienDien());
        assertEquals(new BigDecimal("100000"), saved.getTienNuoc());
        assertEquals(new BigDecimal("3175000"), saved.getTongTien());
        assertEquals("CHUA_THANH_TOAN", saved.getTrangThai());
        assertNull(saved.getNgayThanhToan());
        verify(hoaDonRepository).save(any(HoaDon.class));
    }

    @Test
    void taoHoaDon_chiSoDienMoiNhoHonCu_nemBusinessException() {
        HoaDonCreateDTO dto = validDto();
        dto.setChiSoDienCu(200);
        dto.setChiSoDienMoi(180);

        when(hopDongRepository.findById(10)).thenReturn(Optional.of(hopDongHieuLuc));
        when(hoaDonRepository.existsByHopDongIdAndThangNam(10, "11/2026")).thenReturn(false);

        assertThrows(BusinessException.class, () -> hoaDonService.taoHoaDon(dto));
        verify(hoaDonRepository, never()).save(any());
    }

    @Test
    void taoHoaDon_chiSoNuocAm_nemBusinessException() {
        HoaDonCreateDTO dto = validDto();
        dto.setChiSoNuocCu(-1);
        dto.setChiSoNuocMoi(5);

        when(hopDongRepository.findById(10)).thenReturn(Optional.of(hopDongHieuLuc));
        when(hoaDonRepository.existsByHopDongIdAndThangNam(10, "11/2026")).thenReturn(false);

        assertThrows(BusinessException.class, () -> hoaDonService.taoHoaDon(dto));
        verify(hoaDonRepository, never()).save(any());
    }

    @Test
    void taoHoaDon_trungThangNam_nemBusinessException() {
        when(hopDongRepository.findById(10)).thenReturn(Optional.of(hopDongHieuLuc));
        when(hoaDonRepository.existsByHopDongIdAndThangNam(10, "11/2026")).thenReturn(true);

        assertThrows(BusinessException.class, () -> hoaDonService.taoHoaDon(validDto()));
        verify(hoaDonRepository, never()).save(any());
    }

    @Test
    void taoHoaDon_hopDongDaThanhLy_nemBusinessException() {
        hopDongHieuLuc.setTrangThai("DA_THANH_LY");
        when(hopDongRepository.findById(10)).thenReturn(Optional.of(hopDongHieuLuc));
        when(hoaDonRepository.existsByHopDongIdAndThangNam(10, "11/2026")).thenReturn(false);

        assertThrows(BusinessException.class, () -> hoaDonService.taoHoaDon(validDto()));
        verify(hoaDonRepository, never()).save(any());
    }

    @Test
    void xacNhanThanhToan_chuaThanhToan_chuyenTrangThaiDaThanhToan() {
        HoaDon hd = new HoaDon();
        hd.setId(5);
        hd.setMaHoaDon("HD-112026-P102");
        hd.setTrangThai("CHUA_THANH_TOAN");

        when(hoaDonRepository.findById(5)).thenReturn(Optional.of(hd));

        hoaDonService.xacNhanThanhToan(5);

        assertEquals("DA_THANH_TOAN", hd.getTrangThai());
        assertNotNull(hd.getNgayThanhToan());
        verify(hoaDonRepository).save(hd);
    }

    @Test
    void xacNhanThanhToan_daThanhToanTruocDo_nemBusinessException() {
        HoaDon hd = new HoaDon();
        hd.setId(5);
        hd.setMaHoaDon("HD-112026-P102");
        hd.setTrangThai("DA_THANH_TOAN");

        when(hoaDonRepository.findById(5)).thenReturn(Optional.of(hd));

        assertThrows(BusinessException.class, () -> hoaDonService.xacNhanThanhToan(5));
        verify(hoaDonRepository, never()).save(any());
    }

    @Test
    void huyThanhToan_hoanTacVeChuaThanhToan() {
        HoaDon hd = new HoaDon();
        hd.setId(5);
        hd.setMaHoaDon("HD-112026-P102");
        hd.setTrangThai("DA_THANH_TOAN");

        when(hoaDonRepository.findById(5)).thenReturn(Optional.of(hd));

        hoaDonService.huyThanhToan(5);

        assertEquals("CHUA_THANH_TOAN", hd.getTrangThai());
        assertNull(hd.getNgayThanhToan());
        verify(hoaDonRepository).save(hd);
    }

    @Test
    void xoaHoaDon_chuaThanhToan_xoaThanhCong() {
        HoaDon hd = new HoaDon();
        hd.setId(5);
        hd.setMaHoaDon("HD-112026-P102");
        hd.setTrangThai("CHUA_THANH_TOAN");

        when(hoaDonRepository.findById(5)).thenReturn(Optional.of(hd));

        hoaDonService.xoaHoaDon(5);

        verify(hoaDonRepository).delete(hd);
    }

    @Test
    void xoaHoaDon_daThanhToan_nemBusinessException() {
        HoaDon hd = new HoaDon();
        hd.setId(5);
        hd.setMaHoaDon("HD-112026-P102");
        hd.setTrangThai("DA_THANH_TOAN");

        when(hoaDonRepository.findById(5)).thenReturn(Optional.of(hd));

        assertThrows(BusinessException.class, () -> hoaDonService.xoaHoaDon(5));
        verify(hoaDonRepository, never()).delete(any());
    }
}
