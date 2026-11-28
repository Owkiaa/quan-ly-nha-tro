package com.nhatro.service;

import com.nhatro.dto.DashboardDTO;
import com.nhatro.repository.HoaDonRepository;
import com.nhatro.repository.PhongTroRepository;
import com.nhatro.repository.YeuCauSuaChuaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PhongTroRepository phongTroRepository;

    @Mock
    private HoaDonRepository hoaDonRepository;

    @Mock
    private YeuCauSuaChuaRepository yeuCauSuaChuaRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardStats_tongPhongBangKhong_tiLeLapDayBangKhongVaKhongLoiNull() {
        when(phongTroRepository.countByTrangThai(anyString())).thenReturn(0L);
        when(hoaDonRepository.sumTongDoanhThuDaThu()).thenReturn(null);
        when(hoaDonRepository.sumTongNoChuaThu()).thenReturn(null);
        when(yeuCauSuaChuaRepository.sumTongChiPhi()).thenReturn(null);
        when(hoaDonRepository.countByTrangThai(anyString())).thenReturn(0L);
        when(hoaDonRepository.sumDoanhThuTheoThang(anyString())).thenReturn(null);
        when(hoaDonRepository.sumNoTheoThang(anyString())).thenReturn(null);

        DashboardDTO stats = dashboardService.getDashboardStats();

        assertEquals(0L, stats.getTongSoPhong());
        assertEquals(0.0, stats.getTiLeLapDay());
        assertEquals(BigDecimal.ZERO, stats.getTongDoanhThuDaThu());
        assertEquals(BigDecimal.ZERO, stats.getTongNoChuaThu());
        assertEquals(BigDecimal.ZERO, stats.getTongChiPhiSuaChua());
        assertEquals(BigDecimal.ZERO, stats.getLoiNhuanThucNhan());
        assertEquals(6, stats.getBieuDoThangLabels().size());
        assertEquals(6, stats.getBieuDoDoanhThuValues().size());
        assertEquals(6, stats.getBieuDoNoValues().size());
    }

    @Test
    void getDashboardStats_coDuLieuDayDu_tinhDungTiLeLapDayLoiNhuanVaBieuDo6Thang() {
        when(phongTroRepository.countByTrangThai("TRONG")).thenReturn(4L);
        when(phongTroRepository.countByTrangThai("DA_DAT")).thenReturn(3L);
        when(phongTroRepository.countByTrangThai("DA_THUE")).thenReturn(11L);
        when(phongTroRepository.countByTrangThai("BAO_TRI")).thenReturn(2L);

        when(hoaDonRepository.sumTongDoanhThuDaThu()).thenReturn(new BigDecimal("35000000"));
        when(hoaDonRepository.sumTongNoChuaThu()).thenReturn(new BigDecimal("6200000"));
        when(yeuCauSuaChuaRepository.sumTongChiPhi()).thenReturn(new BigDecimal("1500000"));
        when(hoaDonRepository.countByTrangThai("DA_THANH_TOAN")).thenReturn(10L);
        when(hoaDonRepository.countByTrangThai("CHUA_THANH_TOAN")).thenReturn(2L);
        when(hoaDonRepository.sumDoanhThuTheoThang(anyString())).thenReturn(new BigDecimal("5000000"));
        when(hoaDonRepository.sumNoTheoThang(anyString())).thenReturn(new BigDecimal("1000000"));

        DashboardDTO stats = dashboardService.getDashboardStats();

        assertEquals(20L, stats.getTongSoPhong());
        assertEquals(55.0, stats.getTiLeLapDay());
        assertEquals(new BigDecimal("35000000"), stats.getTongDoanhThuDaThu());
        assertEquals(new BigDecimal("6200000"), stats.getTongNoChuaThu());
        assertEquals(new BigDecimal("1500000"), stats.getTongChiPhiSuaChua());
        assertEquals(new BigDecimal("33500000"), stats.getLoiNhuanThucNhan());
        assertEquals(10L, stats.getSoHoaDonDaThanhToan());
        assertEquals(2L, stats.getSoHoaDonChuaThanhToan());
        assertEquals(6, stats.getBieuDoThangLabels().size());
        assertEquals(4, stats.getBieuDoPhongValues().size());
        assertEquals(11L, stats.getBieuDoPhongValues().get(0));
    }
}
