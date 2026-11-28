package com.nhatro.service;

import com.nhatro.dto.HopDongRequestDTO;
import com.nhatro.dto.QuyetToanHopDongDTO;
import com.nhatro.entity.DatCho;
import com.nhatro.entity.HoaDon;
import com.nhatro.entity.HopDong;
import com.nhatro.entity.KhachThue;
import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.repository.DatChoRepository;
import com.nhatro.repository.HoaDonRepository;
import com.nhatro.repository.HopDongRepository;
import com.nhatro.repository.KhachThueRepository;
import com.nhatro.repository.PhongTroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HopDongServiceTest {

    @Mock
    private HopDongRepository hopDongRepository;

    @Mock
    private PhongTroRepository phongTroRepository;

    @Mock
    private KhachThueRepository khachThueRepository;

    @Mock
    private DatChoRepository datChoRepository;

    @Mock
    private HoaDonRepository hoaDonRepository;

    @InjectMocks
    private HopDongService hopDongService;

    private PhongTro phong;
    private KhachThue khachDat;
    private KhachThue khachKhac;

    @BeforeEach
    void setUp() {
        phong = new PhongTro();
        phong.setId(1);
        phong.setMaPhong("P201");
        phong.setTenPhong("Phòng 201");
        phong.setGiaThue(new BigDecimal("3000000"));
        phong.setTrangThai("TRONG");

        khachDat = new KhachThue();
        khachDat.setId(10);
        khachDat.setHoTen("Nguyễn Văn An");
        khachDat.setCccd("079204000001");

        khachKhac = new KhachThue();
        khachKhac.setId(20);
        khachKhac.setHoTen("Trần Thị Bình");
        khachKhac.setCccd("079204000002");
    }

    @Test
    void taoHopDong_WithBookedRoomByDifferentCustomer_ThrowsBusinessException() {
        phong.setTrangThai(HopDongService.PHONG_DA_DAT);
        DatCho datCho = new DatCho();
        datCho.setId(5);
        datCho.setPhongTro(phong);
        datCho.setKhachThue(khachDat);
        datCho.setTrangThai(HopDongService.DAT_CHO_DANG_GIU);

        when(phongTroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(phong));
        when(khachThueRepository.findById(20)).thenReturn(Optional.of(khachKhac));
        when(datChoRepository.findFirstByPhongTroIdAndTrangThai(1, HopDongService.DAT_CHO_DANG_GIU))
                .thenReturn(Optional.of(datCho));

        HopDongRequestDTO req = new HopDongRequestDTO(
                null, "HD-P201-01", 1, 20,
                LocalDate.now(), LocalDate.now().plusMonths(6),
                new BigDecimal("3000000"), new BigDecimal("3000000"), 1, null
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> hopDongService.taoHopDong(req));
        assertTrue(ex.getMessage().contains("giữ chỗ"));
        verify(hopDongRepository, never()).save(any());
    }

    @Test
    void taoHopDong_WithBookedRoomBySameCustomer_ConvertsBookingAndRentsRoom() {
        phong.setTrangThai(HopDongService.PHONG_DA_DAT);
        DatCho datCho = new DatCho();
        datCho.setId(5);
        datCho.setPhongTro(phong);
        datCho.setKhachThue(khachDat);
        datCho.setTrangThai(HopDongService.DAT_CHO_DANG_GIU);

        when(phongTroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(phong));
        when(khachThueRepository.findById(10)).thenReturn(Optional.of(khachDat));
        when(hopDongRepository.existsByMaHopDong("HD-P201-01")).thenReturn(false);
        when(datChoRepository.findFirstByPhongTroIdAndTrangThai(1, HopDongService.DAT_CHO_DANG_GIU))
                .thenReturn(Optional.of(datCho));
        when(hopDongRepository.save(any(HopDong.class))).thenAnswer(inv -> inv.getArgument(0));

        HopDongRequestDTO req = new HopDongRequestDTO(
                null, "HD-P201-01", 1, 10,
                LocalDate.now(), LocalDate.now().plusMonths(6),
                new BigDecimal("3000000"), new BigDecimal("3000000"), 1, null
        );

        HopDong result = hopDongService.taoHopDong(req);

        assertEquals(HopDongService.DAT_CHO_DA_CHUYEN_HOP_DONG, datCho.getTrangThai());
        assertEquals(HopDongService.PHONG_DA_THUE, phong.getTrangThai());
        assertEquals(HopDongService.TRANG_THAI_HIEU_LUC, result.getTrangThai());
        verify(datChoRepository).save(datCho);
        verify(phongTroRepository).save(phong);
        verify(hopDongRepository).save(any(HopDong.class));
    }

    @Test
    void xoaHopDong_WhenHasInvoices_ThrowsBusinessException() {
        HopDong hd = new HopDong();
        hd.setId(1);
        hd.setMaHopDong("HD-P201-01");

        when(hopDongRepository.findById(1)).thenReturn(Optional.of(hd));
        when(hoaDonRepository.existsByHopDongId(1)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> hopDongService.xoaHopDong(1));
        assertEquals("Không thể xóa hợp đồng đã phát sinh hóa đơn thu tiền. Vui lòng chuyển trạng thái sang Đã thanh lý.",
                ex.getMessage());
        verify(hopDongRepository, never()).delete(any());
    }

    @Test
    void thanhLyKemQuyetToan_DeductsUnpaidInvoicesAndFreesRoom() {
        phong.setTrangThai(HopDongService.PHONG_DA_THUE);
        HopDong hd = new HopDong();
        hd.setId(1);
        hd.setMaHopDong("HD-P201-01");
        hd.setPhongTro(phong);
        hd.setKhachThue(khachDat);
        hd.setTienDatCoc(new BigDecimal("3000000"));
        hd.setTrangThai(HopDongService.TRANG_THAI_HIEU_LUC);

        HoaDon hdNo = new HoaDon();
        hdNo.setId(99);
        hdNo.setTongTien(new BigDecimal("850000"));
        hdNo.setTrangThai("CHUA_THANH_TOAN");

        QuyetToanHopDongDTO req = new QuyetToanHopDongDTO();
        req.setChiPhiKhauTru(new BigDecimal("150000"));
        req.setLyDoKhauTru("Thay ổ khóa");
        req.setGachNoHoaDon(true);

        when(hopDongRepository.findById(1)).thenReturn(Optional.of(hd));
        when(hoaDonRepository.findByHopDongIdAndTrangThai(1, "CHUA_THANH_TOAN")).thenReturn(List.of(hdNo));
        when(phongTroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(phong));
        when(hopDongRepository.save(any(HopDong.class))).thenAnswer(inv -> inv.getArgument(0));
        when(hopDongRepository.existsByKhachThueIdAndTrangThai(10, HopDongService.TRANG_THAI_HIEU_LUC)).thenReturn(false);

        QuyetToanHopDongDTO result = hopDongService.thanhLyKemQuyetToan(1, req);

        assertEquals(new BigDecimal("2000000"), result.getTienThucHoan());
        assertEquals("DA_THANH_TOAN", hdNo.getTrangThai());
        assertEquals(HopDongService.TRANG_THAI_DA_THANH_LY, hd.getTrangThai());
        assertEquals(HopDongService.PHONG_TRONG, phong.getTrangThai());
        verify(hoaDonRepository).save(hdNo);
    }

    @Test
    void markContractExpired_WhenPastEndDate_UpdatesStatusToHetHan() {
        HopDong hd = new HopDong();
        hd.setId(1);
        hd.setMaHopDong("HD-P201-01");
        hd.setPhongTro(phong);
        hd.setTrangThai(HopDongService.TRANG_THAI_HIEU_LUC);
        hd.setNgayKetThuc(LocalDate.now().minusDays(2));

        when(hopDongRepository.findById(1)).thenReturn(Optional.of(hd));

        hopDongService.markContractExpired(1);

        assertEquals(HopDongService.TRANG_THAI_HET_HAN, hd.getTrangThai());
        verify(hopDongRepository).save(hd);
    }
}
