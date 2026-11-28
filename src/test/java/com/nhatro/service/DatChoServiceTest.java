package com.nhatro.service;

import com.nhatro.entity.DatCho;
import com.nhatro.entity.KhachThue;
import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.RoomAlreadyBookedException;
import com.nhatro.repository.DatChoRepository;
import com.nhatro.repository.KhachThueRepository;
import com.nhatro.repository.PhongTroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatChoServiceTest {

    @Mock
    private PhongTroRepository phongTroRepository;

    @Mock
    private DatChoRepository datChoRepository;

    @Mock
    private KhachThueRepository khachThueRepository;

    @InjectMocks
    private DatChoService datChoService;

    private PhongTro phongTrong;
    private KhachThue khachThue;

    @BeforeEach
    void setUp() {
        phongTrong = new PhongTro();
        phongTrong.setId(1);
        phongTrong.setMaPhong("P201");
        phongTrong.setTenPhong("Phòng 201");
        phongTrong.setGiaThue(new BigDecimal("3000000"));
        phongTrong.setTrangThai("TRONG");

        khachThue = new KhachThue();
        khachThue.setId(10);
        khachThue.setHoTen("Nguyễn Văn An");
        khachThue.setCccd("079204000001");
        khachThue.setSoDienThoai("0912345678");
        khachThue.setTrangThai("DANG_THUE");
    }

    @Test
    void datChoPhong_Success_LocksRoomAndCreatesBooking() {
        when(phongTroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(phongTrong));
        when(datChoRepository.countByKhachThueCccdAndTrangThai("079204000001", DatChoService.TRANG_THAI_DANG_GIU))
                .thenReturn(0L);
        when(khachThueRepository.findByCccd("079204000001")).thenReturn(Optional.of(khachThue));
        when(datChoRepository.save(any(DatCho.class))).thenAnswer(inv -> {
            DatCho dc = inv.getArgument(0);
            dc.setId(100);
            return dc;
        });

        DatCho result = datChoService.datChoPhong(1, "Nguyễn Văn An", "079204000001", "0912345678", null, "Test", 24);

        assertNotNull(result);
        assertEquals(100, result.getId());
        assertEquals(DatChoService.TRANG_THAI_DANG_GIU, result.getTrangThai());
        assertEquals(DatChoService.PHONG_DA_DAT, phongTrong.getTrangThai());
        verify(phongTroRepository).findByIdForUpdate(1);
        verify(phongTroRepository).save(phongTrong);
        verify(datChoRepository).save(any(DatCho.class));
    }

    @Test
    void datChoPhong_WhenRoomNotTrong_ThrowsRoomAlreadyBookedException() {
        phongTrong.setTrangThai("DA_DAT");
        when(phongTroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(phongTrong));

        assertThrows(RoomAlreadyBookedException.class, () ->
                datChoService.datChoPhong(1, "Nguyễn Văn An", "079204000001", "0912345678", null, null, 24));

        verify(datChoRepository, never()).save(any());
    }

    @Test
    void datChoPhong_WhenExceedsMaxActiveBookingsPerCccd_ThrowsBusinessException() {
        when(phongTroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(phongTrong));
        when(datChoRepository.countByKhachThueCccdAndTrangThai("079204000001", DatChoService.TRANG_THAI_DANG_GIU))
                .thenReturn((long) DatChoService.MAX_ACTIVE_BOOKINGS_PER_CCCD);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                datChoService.datChoPhong(1, "Nguyễn Văn An", "079204000001", "0912345678", null, null, 24));

        assertTrue(ex.getMessage().contains("tối đa"));
        verify(datChoRepository, never()).save(any());
    }

    @Test
    void releaseSingleBooking_WhenDangGiu_ReleasesRoomToTrong() {
        phongTrong.setTrangThai(DatChoService.PHONG_DA_DAT);
        DatCho datCho = new DatCho();
        datCho.setId(5);
        datCho.setPhongTro(phongTrong);
        datCho.setKhachThue(khachThue);
        datCho.setTrangThai(DatChoService.TRANG_THAI_DANG_GIU);
        datCho.setHanGiuCho(LocalDateTime.now().minusHours(1));

        when(phongTroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(phongTrong));
        when(datChoRepository.findById(5)).thenReturn(Optional.of(datCho));

        datChoService.releaseSingleBooking(5, 1);

        assertEquals(DatChoService.TRANG_THAI_HET_HAN, datCho.getTrangThai());
        assertEquals(DatChoService.PHONG_TRONG, phongTrong.getTrangThai());
        verify(datChoRepository).save(datCho);
        verify(phongTroRepository).save(phongTrong);
    }

    @Test
    void giaHanDatCho_WhenDangGiu_ExtendsHanGiuCho() {
        LocalDateTime hanCu = LocalDateTime.now().plusHours(10);
        DatCho datCho = new DatCho();
        datCho.setId(5);
        datCho.setPhongTro(phongTrong);
        datCho.setKhachThue(khachThue);
        datCho.setTrangThai(DatChoService.TRANG_THAI_DANG_GIU);
        datCho.setHanGiuCho(hanCu);

        when(datChoRepository.findById(5)).thenReturn(Optional.of(datCho));
        when(datChoRepository.save(any(DatCho.class))).thenAnswer(inv -> inv.getArgument(0));

        DatCho updated = datChoService.giaHanDatCho(5, 24);

        assertEquals(hanCu.plusHours(24), updated.getHanGiuCho());
        assertTrue(updated.getGhiChu().contains("Gia hạn +24h"));
    }
}
