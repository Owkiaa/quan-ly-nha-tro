package com.nhatro.service;

import com.nhatro.dto.TenantRegisterDTO;
import com.nhatro.entity.DatCho;
import com.nhatro.entity.HoaDon;
import com.nhatro.entity.HopDong;
import com.nhatro.entity.KhachThue;
import com.nhatro.entity.PhongTro;
import com.nhatro.entity.TaiKhoan;
import com.nhatro.entity.YeuCauSuaChua;
import com.nhatro.repository.DatChoRepository;
import com.nhatro.repository.KhachThueRepository;
import com.nhatro.repository.PhongTroRepository;
import com.nhatro.repository.TaiKhoanRepository;
import com.nhatro.repository.TenantBookingRepository;
import com.nhatro.repository.TenantContractRepository;
import com.nhatro.repository.TenantIncidentRepository;
import com.nhatro.repository.TenantInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test nghiệp vụ Cổng Khách thuê. Toàn bộ repository và DatChoService được giả lập (Mockito),
 * không cần CSDL: kiểm tra logic của TenantPortalService, đặc biệt việc luôn truyền khachId vào truy vấn.
 */
@ExtendWith(MockitoExtension.class)
class TenantPortalServiceTest {

    @Mock private PhongTroRepository phongTroRepository;
    @Mock private KhachThueRepository khachThueRepository;
    @Mock private TaiKhoanRepository taiKhoanRepository;
    @Mock private TenantContractRepository contractRepository;
    @Mock private TenantInvoiceRepository invoiceRepository;
    @Mock private TenantIncidentRepository incidentRepository;
    @Mock private DatChoRepository datChoRepository;
    @Mock private TenantBookingRepository bookingRepository;
    @Mock private DatChoService datChoService;

    private TenantPortalService service;

    @BeforeEach
    void setUp() {
        service = new TenantPortalService(phongTroRepository, khachThueRepository, taiKhoanRepository,
                contractRepository, invoiceRepository, incidentRepository,
                datChoRepository, bookingRepository, datChoService);
    }

    // ================================================================== dữ liệu mẫu

    private static PhongTro phong(int id, String ma, String trangThai) {
        PhongTro p = new PhongTro();
        p.setId(id);
        p.setMaPhong(ma);
        p.setTenPhong("Phòng " + ma);
        p.setTrangThai(trangThai);
        return p;
    }

    private static KhachThue khach(int id, String cccd, String sdt) {
        KhachThue k = new KhachThue();
        k.setId(id);
        k.setHoTen("Khách " + id);
        k.setCccd(cccd);
        k.setSoDienThoai(sdt);
        k.setTrangThai("DANG_THUE");
        return k;
    }

    private static HopDong hopDong(int id, KhachThue k, PhongTro p, String trangThai) {
        HopDong h = new HopDong();
        h.setId(id);
        h.setKhachThue(k);
        h.setPhongTro(p);
        h.setTrangThai(trangThai);
        return h;
    }

    private static DatCho datCho(int id, KhachThue k, PhongTro p, String trangThai) {
        DatCho d = new DatCho();
        d.setId(id);
        d.setKhachThue(k);
        d.setPhongTro(p);
        d.setTrangThai(trangThai);
        d.setHanGiuCho(LocalDateTime.now().plusHours(24));
        return d;
    }

    private static TenantRegisterDTO dtoHopLe() {
        TenantRegisterDTO dto = new TenantRegisterDTO();
        dto.setTenDangNhap("tenant99");
        dto.setMatKhau("Secret1");
        dto.setXacNhanMatKhau("Secret1");
        dto.setHoTen("Nguyễn Văn Mới");
        dto.setCccd("012345678901");
        dto.setSoDienThoai("0912345678");
        dto.setEmail("moi@example.com");
        return dto;
    }

    // ================================================================== 1. LỌC PHÒNG TRỐNG

    @Test
    @DisplayName("Lọc phòng: truyền đủ bộ lọc xuống repository, keyword được trim")
    void locPhong_truyenDayDuBoLoc() {
        Page<PhongTro> ketQua = new PageImpl<>(List.of(phong(1, "P101", "TRONG")));
        when(phongTroRepository.searchAvailable(any(), any(), any(), any(), any(), any())).thenReturn(ketQua);

        Page<PhongTro> out = service.searchAvailableRooms("  p1 ", "Đơn",
                new BigDecimal("2000000"), new BigDecimal("3000000"), 20.0,
                PageRequest.of(0, 8, Sort.by("giaThue")));

        assertSame(ketQua, out);
        verify(phongTroRepository).searchAvailable(eq("p1"), eq("Đơn"),
                eq(new BigDecimal("2000000")), eq(new BigDecimal("3000000")), eq(20.0), any(Pageable.class));
    }

    @Test
    @DisplayName("Lọc phòng: bộ lọc null -> keyword/loại = null, giá 0..cận trên, diện tích 0")
    void locPhong_boLocNull_dungGiaTriMacDinh() {
        when(phongTroRepository.searchAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        service.searchAvailableRooms(null, null, null, null, null, PageRequest.of(0, 8));

        verify(phongTroRepository).searchAvailable(isNull(), isNull(), eq(BigDecimal.ZERO),
                eq(new BigDecimal("9999999999")), eq(0.0), any(Pageable.class));
    }

    @Test
    @DisplayName("Lọc phòng: chuỗi rỗng/khoảng trắng coi như không lọc")
    void locPhong_chuoiRong_thanhNull() {
        when(phongTroRepository.searchAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        service.searchAvailableRooms("   ", "", null, null, null, PageRequest.of(0, 8));

        verify(phongTroRepository).searchAvailable(isNull(), isNull(), any(BigDecimal.class),
                any(BigDecimal.class), any(Double.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Lọc phòng: giá từ > giá đến thì đổi chỗ")
    void locPhong_giaTuLonHonGiaDen_doiCho() {
        when(phongTroRepository.searchAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        service.searchAvailableRooms(null, null, new BigDecimal("3500000"), new BigDecimal("2000000"),
                null, PageRequest.of(0, 8));

        verify(phongTroRepository).searchAvailable(isNull(), isNull(), eq(new BigDecimal("2000000")),
                eq(new BigDecimal("3500000")), eq(0.0), any(Pageable.class));
    }

    @Test
    @DisplayName("Lọc phòng: giá đến <= 0 là không giới hạn, giá từ và diện tích âm coi như 0")
    void locPhong_giaTriAm_duocChuanHoa() {
        when(phongTroRepository.searchAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        service.searchAvailableRooms(null, null, new BigDecimal("-5"), BigDecimal.ZERO, -3.0,
                PageRequest.of(0, 8));

        verify(phongTroRepository).searchAvailable(isNull(), isNull(), eq(BigDecimal.ZERO),
                eq(new BigDecimal("9999999999")), eq(0.0), any(Pageable.class));
    }

    @Test
    @DisplayName("Lọc phòng: trường sắp xếp lạ bị thay bằng giá thuê tăng dần, cỡ trang bị chặn ở 50")
    void locPhong_sapXepLa_vaCoTrangQuaLon() {
        when(phongTroRepository.searchAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        service.searchAvailableRooms(null, null, null, null, null,
                PageRequest.of(2, 500, Sort.by(Sort.Direction.DESC, "trangThai")));

        verify(phongTroRepository).searchAvailable(any(), any(), any(), any(), any(), captor.capture());
        Pageable used = captor.getValue();
        assertEquals(2, used.getPageNumber());
        assertEquals(50, used.getPageSize());
        assertNotNull(used.getSort().getOrderFor("giaThue"));
        assertTrue(used.getSort().getOrderFor("giaThue").isAscending());
        assertNull(used.getSort().getOrderFor("trangThai"));
    }

    @Test
    @DisplayName("Lọc phòng: trường sắp xếp hợp lệ được giữ nguyên chiều")
    void locPhong_sapXepHopLe_giuNguyen() {
        when(phongTroRepository.searchAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        service.searchAvailableRooms(null, null, null, null, null,
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "dienTich")));

        verify(phongTroRepository).searchAvailable(any(), any(), any(), any(), any(), captor.capture());
        assertTrue(captor.getValue().getSort().getOrderFor("dienTich").isDescending());
    }

    // ================================================================== 2. ĐẶT GIỮ CHỖ

    @Test
    @DisplayName("Giữ chỗ: dưới hạn mức thì gọi DatChoService với khachId từ tham số, mặc định 24 giờ")
    void giuCho_duoiHanMuc_goiDatChoService() {
        DatCho ketQua = new DatCho();
        when(khachThueRepository.existsById(1)).thenReturn(true);
        when(bookingRepository.countByKhachThueIdAndTrangThai(1, "DANG_GIU")).thenReturn(1L);
        when(datChoService.datChoPhong(1, 5, 24, null)).thenReturn(ketQua);

        DatCho out = service.bookRoomForTenant(1, 5, null, "   ");

        assertSame(ketQua, out);
        verify(datChoService).datChoPhong(1, 5, 24, null);
    }

    @Test
    @DisplayName("Giữ chỗ: đã giữ 2 phòng thì chặn, không gọi DatChoService")
    void giuCho_da2Phong_biChan() {
        when(khachThueRepository.existsById(1)).thenReturn(true);
        when(bookingRepository.countByKhachThueIdAndTrangThai(1, "DANG_GIU")).thenReturn(2L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.bookRoomForTenant(1, 5, 24, null));

        assertTrue(ex.getMessage().contains("2"));
        verifyNoInteractions(datChoService);
    }

    @Test
    @DisplayName("Giữ chỗ: vượt quá 2 phòng (dữ liệu lệch) cũng bị chặn")
    void giuCho_tren2Phong_biChan() {
        when(khachThueRepository.existsById(1)).thenReturn(true);
        when(bookingRepository.countByKhachThueIdAndTrangThai(1, "DANG_GIU")).thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> service.bookRoomForTenant(1, 5, 24, null));
        verifyNoInteractions(datChoService);
    }

    @ParameterizedTest(name = "soGioGiuCho = {0} bị từ chối")
    @ValueSource(ints = {0, -1, 73, 1000})
    @DisplayName("Giữ chỗ: số giờ ngoài khoảng 1-72 bị từ chối")
    void giuCho_soGioNgoaiKhoang_biTuChoi(int soGio) {
        assertThrows(IllegalArgumentException.class, () -> service.bookRoomForTenant(1, 5, soGio, null));
        verifyNoInteractions(datChoService);
    }

    @Test
    @DisplayName("Giữ chỗ: ghi chú quá 255 ký tự bị từ chối")
    void giuCho_ghiChuQuaDai_biTuChoi() {
        String dai = "x".repeat(256);
        assertThrows(IllegalArgumentException.class, () -> service.bookRoomForTenant(1, 5, 24, dai));
        verifyNoInteractions(datChoService);
    }

    @Test
    @DisplayName("Giữ chỗ: thiếu khachId hoặc phongId bị từ chối")
    void giuCho_thieuThongTin_biTuChoi() {
        assertThrows(IllegalArgumentException.class, () -> service.bookRoomForTenant(null, 5, 24, null));
        assertThrows(IllegalArgumentException.class, () -> service.bookRoomForTenant(1, null, 24, null));
        verifyNoInteractions(datChoService);
    }

    @Test
    @DisplayName("Giữ chỗ: khách không tồn tại bị từ chối")
    void giuCho_khachKhongTonTai_biTuChoi() {
        when(khachThueRepository.existsById(99)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.bookRoomForTenant(99, 5, 24, null));
        verifyNoInteractions(datChoService);
    }

    @Test
    @DisplayName("Giữ chỗ: lỗi nghiệp vụ từ DatChoService được giữ nguyên thông báo")
    void giuCho_loiNghiepVu_giuNguyenThongBao() {
        when(khachThueRepository.existsById(1)).thenReturn(true);
        when(bookingRepository.countByKhachThueIdAndTrangThai(1, "DANG_GIU")).thenReturn(0L);
        when(datChoService.datChoPhong(1, 5, 24, null))
                .thenThrow(new IllegalStateException("Phòng đã có người giữ chỗ"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.bookRoomForTenant(1, 5, 24, null));

        assertEquals("Phòng đã có người giữ chỗ", ex.getMessage());
    }

    @Test
    @DisplayName("Giữ chỗ: lỗi hạ tầng bị che chi tiết, chỉ báo thông báo chung")
    void giuCho_loiHaTang_thongBaoChung() {
        when(khachThueRepository.existsById(1)).thenReturn(true);
        when(bookingRepository.countByKhachThueIdAndTrangThai(1, "DANG_GIU")).thenReturn(0L);
        when(datChoService.datChoPhong(1, 5, 24, null))
                .thenThrow(new RuntimeException("SQL: deadlock victim table dat_cho"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.bookRoomForTenant(1, 5, 24, null));

        assertFalse(ex.getMessage().contains("SQL"));
    }

    // ================================================================== 3. CÔ LẬP DỮ LIỆU GIỮA CÁC KHÁCH

    @Test
    @DisplayName("Cô lập: hợp đồng chỉ lấy theo khachId của người đang đăng nhập")
    void coLap_hopDong_theoKhachId() {
        KhachThue k1 = khach(1, "111111111", "0900000001");
        KhachThue k2 = khach(2, "222222222", "0900000002");
        HopDong h1 = hopDong(10, k1, phong(1, "P101", "DA_THUE"), "HIEU_LUC");
        HopDong h2 = hopDong(20, k2, phong(2, "P102", "DA_THUE"), "HIEU_LUC");
        when(contractRepository.findAllByKhach(1)).thenReturn(List.of(h1));
        when(contractRepository.findAllByKhach(2)).thenReturn(List.of(h2));

        assertEquals(List.of(h1), service.getContracts(1));
        assertEquals(List.of(h2), service.getContracts(2));
        verify(contractRepository, never()).findAllByKhach(3);
    }

    @Test
    @DisplayName("Cô lập: danh sách hóa đơn luôn truyền khachId vào truy vấn")
    void coLap_danhSachHoaDon_theoKhachId() {
        HoaDon a = new HoaDon();
        a.setId(1);
        HoaDon b = new HoaDon();
        b.setId(2);
        when(invoiceRepository.findByKhach(eq(1), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a)));
        when(invoiceRepository.findByKhach(eq(2), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(b)));

        assertEquals(List.of(a), service.getInvoices(1, "", PageRequest.of(0, 10)).getContent());
        assertEquals(List.of(b), service.getInvoices(2, null, PageRequest.of(0, 10)).getContent());
    }

    @Test
    @DisplayName("Cô lập: khách B không xem được hóa đơn của khách A dù biết id")
    void coLap_chiTietHoaDon_khachKhacKhongThay() {
        HoaDon cuaKhach1 = new HoaDon();
        cuaKhach1.setId(10);
        when(invoiceRepository.findByIdAndKhach(10, 1)).thenReturn(Optional.of(cuaKhach1));
        when(invoiceRepository.findByIdAndKhach(10, 2)).thenReturn(Optional.empty());

        assertTrue(service.findInvoice(1, 10).isPresent());
        assertTrue(service.findInvoice(2, 10).isEmpty());
    }

    @Test
    @DisplayName("Cô lập: phiếu đặt chỗ liệt kê theo từng khách")
    void coLap_phieuDatCho_theoKhachId() {
        DatCho d1 = datCho(1, khach(1, "111111111", "0900000001"), phong(1, "P101", "DA_DAT"), "DANG_GIU");
        DatCho d2 = datCho(2, khach(2, "222222222", "0900000002"), phong(2, "P102", "DA_DAT"), "DANG_GIU");
        when(bookingRepository.findAllByKhach(1)).thenReturn(List.of(d1));
        when(bookingRepository.findAllByKhach(2)).thenReturn(List.of(d2));

        assertEquals(List.of(d1), service.getBookingsByKhach(1));
        assertEquals(List.of(d2), service.getBookingsByKhach(2));
    }

    @Test
    @DisplayName("Chống IDOR: khách B hủy phiếu của khách A bị từ chối, không đụng tới phòng/phiếu")
    void idor_huyPhieuCuaNguoiKhac_biTuChoi() {
        when(bookingRepository.findPhongIdByIdAndKhach(7, 2)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.cancelBookingByKhach(2, 7));

        assertEquals(TenantPortalService.MSG_KHONG_TIM_THAY_PHIEU, ex.getMessage());
        verify(phongTroRepository, never()).findByIdForUpdate(any());
        verify(bookingRepository, never()).save(any());
        verify(phongTroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hủy phiếu: đúng chủ -> phiếu HET_HAN có ghi chú, phòng DA_DAT trở về TRONG")
    void huyPhieu_dungChu_thanhCong() {
        PhongTro phong = phong(5, "P201", "DA_DAT");
        DatCho phieu = datCho(7, khach(1, "111111111", "0900000001"), phong, "DANG_GIU");
        phieu.setGhiChu("Muốn xem phòng chiều");
        when(bookingRepository.findPhongIdByIdAndKhach(7, 1)).thenReturn(Optional.of(5));
        when(phongTroRepository.findByIdForUpdate(5)).thenReturn(Optional.of(phong));
        when(bookingRepository.findByIdAndKhach(7, 1)).thenReturn(Optional.of(phieu));
        when(datChoRepository.existsByPhongTroIdAndTrangThai(5, "DANG_GIU")).thenReturn(false);

        service.cancelBookingByKhach(1, 7);

        assertEquals("HET_HAN", phieu.getTrangThai());
        assertTrue(phieu.getGhiChu().startsWith("Muốn xem phòng chiều"));
        assertTrue(phieu.getGhiChu().contains("Khách tự hủy"));
        assertTrue(phieu.getGhiChu().length() <= 255);
        assertEquals("TRONG", phong.getTrangThai());
        verify(bookingRepository).save(phieu);
        verify(phongTroRepository).save(phong);
    }

    @Test
    @DisplayName("Hủy phiếu: phiếu không còn DANG_GIU thì không hủy được và không đổi phòng")
    void huyPhieu_khongConDangGiu_biTuChoi() {
        PhongTro phong = phong(5, "P201", "TRONG");
        DatCho phieu = datCho(7, khach(1, "111111111", "0900000001"), phong, "HET_HAN");
        when(bookingRepository.findPhongIdByIdAndKhach(7, 1)).thenReturn(Optional.of(5));
        when(phongTroRepository.findByIdForUpdate(5)).thenReturn(Optional.of(phong));
        when(bookingRepository.findByIdAndKhach(7, 1)).thenReturn(Optional.of(phieu));

        assertThrows(IllegalStateException.class, () -> service.cancelBookingByKhach(1, 7));

        assertEquals("HET_HAN", phieu.getTrangThai());
        verify(bookingRepository, never()).save(any());
        verify(phongTroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hủy phiếu: còn phiếu DANG_GIU khác giữ phòng thì phòng giữ nguyên DA_DAT")
    void huyPhieu_conPhieuKhacGiuPhong_phongGiuNguyen() {
        PhongTro phong = phong(5, "P201", "DA_DAT");
        DatCho phieu = datCho(7, khach(1, "111111111", "0900000001"), phong, "DANG_GIU");
        when(bookingRepository.findPhongIdByIdAndKhach(7, 1)).thenReturn(Optional.of(5));
        when(phongTroRepository.findByIdForUpdate(5)).thenReturn(Optional.of(phong));
        when(bookingRepository.findByIdAndKhach(7, 1)).thenReturn(Optional.of(phieu));
        when(datChoRepository.existsByPhongTroIdAndTrangThai(5, "DANG_GIU")).thenReturn(true);

        service.cancelBookingByKhach(1, 7);

        assertEquals("HET_HAN", phieu.getTrangThai());
        assertEquals("DA_DAT", phong.getTrangThai());
        verify(phongTroRepository, never()).save(any());
    }

    // ================================================================== 4. BÁO SỰ CỐ

    @Test
    @DisplayName("Sự cố: có hợp đồng HIEU_LUC của phòng đó thì tạo phiếu CHO_TIEP_NHAN")
    void suCo_coHopDongHieuLuc_taoPhieu() {
        KhachThue k = khach(1, "111111111", "0900000001");
        PhongTro p = phong(5, "P201", "DA_THUE");
        when(khachThueRepository.findById(1)).thenReturn(Optional.of(k));
        when(contractRepository.findActiveByKhachAndPhong(1, 5))
                .thenReturn(List.of(hopDong(10, k, p, "HIEU_LUC")));
        when(incidentRepository.save(any(YeuCauSuaChua.class))).thenAnswer(inv -> inv.getArgument(0));

        YeuCauSuaChua yc = service.createIncident(1, 5, "  Vòi sen bị tắc nước  ", "CAP_BACH");

        assertEquals("CHO_TIEP_NHAN", yc.getTrangThai());
        assertEquals("CAP_BACH", yc.getMucDo());
        assertEquals("Vòi sen bị tắc nước", yc.getNoiDung());
        assertEquals(BigDecimal.ZERO, yc.getChiPhi());
        assertSame(p, yc.getPhongTro());
        assertSame(k, yc.getKhachThue());
        assertNotNull(yc.getNgayTao());
    }

    @Test
    @DisplayName("Sự cố: không có hợp đồng nào của khách ở phòng đó thì từ chối")
    void suCo_khongCoHopDong_biTuChoi() {
        when(khachThueRepository.findById(1)).thenReturn(Optional.of(khach(1, "111111111", "0900000001")));
        when(contractRepository.findActiveByKhachAndPhong(1, 6)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createIncident(1, 6, "Bóng đèn phòng khách cháy", "BINH_THUONG"));

        assertEquals(TenantPortalService.MSG_KHONG_CO_HOP_DONG, ex.getMessage());
        verify(incidentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sự cố: hợp đồng đã HET_HAN/DA_THANH_LY không được báo sự cố")
    void suCo_hopDongKhongHieuLuc_biTuChoi() {
        KhachThue k = khach(1, "111111111", "0900000001");
        PhongTro p = phong(5, "P201", "TRONG");
        when(khachThueRepository.findById(1)).thenReturn(Optional.of(k));
        when(contractRepository.findActiveByKhachAndPhong(1, 5))
                .thenReturn(List.of(hopDong(10, k, p, "HET_HAN"), hopDong(11, k, p, "DA_THANH_LY")));

        assertThrows(IllegalArgumentException.class,
                () -> service.createIncident(1, 5, "Cửa sổ bị kẹt không đóng được", "BINH_THUONG"));
        verify(incidentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sự cố: khách không tồn tại thì từ chối")
    void suCo_khachKhongTonTai_biTuChoi() {
        when(khachThueRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.createIncident(99, 5, "Máy lạnh không mát", "BINH_THUONG"));
        verify(incidentRepository, never()).save(any());
    }

    // ================================================================== 5. TỰ ĐĂNG KÝ TÀI KHOẢN

    @Test
    @DisplayName("Đăng ký: CCCD mới -> tạo hồ sơ khách + tài khoản TENANT, mật khẩu được băm")
    void dangKy_cccdMoi_taoKhachVaTaiKhoan() {
        when(taiKhoanRepository.existsByTenDangNhap("tenant99")).thenReturn(false);
        when(khachThueRepository.findByCccd("012345678901")).thenReturn(Optional.empty());
        when(khachThueRepository.saveAndFlush(any(KhachThue.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taiKhoanRepository.saveAndFlush(any(TaiKhoan.class))).thenAnswer(inv -> inv.getArgument(0));

        TaiKhoan tk = service.registerTenantAccount(dtoHopLe());

        ArgumentCaptor<KhachThue> khachCaptor = ArgumentCaptor.forClass(KhachThue.class);
        verify(khachThueRepository).saveAndFlush(khachCaptor.capture());
        assertEquals("012345678901", khachCaptor.getValue().getCccd());
        assertEquals("0912345678", khachCaptor.getValue().getSoDienThoai());
        assertEquals("DA_TRA_PHONG", khachCaptor.getValue().getTrangThai());

        assertEquals("tenant99", tk.getTenDangNhap());
        assertEquals("TENANT", tk.getVaiTro());
        assertEquals(Boolean.TRUE, tk.getTrangThai());
        assertSame(khachCaptor.getValue(), tk.getKhachThue());
        assertNotEquals("Secret1", tk.getMatKhau());
        assertFalse(tk.getMatKhau().contains("Secret1"));
    }

    @Test
    @DisplayName("Đăng ký: CCCD đã có, SĐT khớp, chưa có tài khoản -> liên kết hồ sơ cũ")
    void dangKy_hoSoCoSanKhop_lienKetHoSoCu() {
        KhachThue hoSoCu = khach(4, "012345678901", "0912345678");
        when(taiKhoanRepository.existsByTenDangNhap("tenant99")).thenReturn(false);
        when(khachThueRepository.findByCccd("012345678901")).thenReturn(Optional.of(hoSoCu));
        when(taiKhoanRepository.existsByKhachThueId(4)).thenReturn(false);
        when(taiKhoanRepository.saveAndFlush(any(TaiKhoan.class))).thenAnswer(inv -> inv.getArgument(0));

        TaiKhoan tk = service.registerTenantAccount(dtoHopLe());

        assertSame(hoSoCu, tk.getKhachThue());
        verify(khachThueRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Đăng ký: CCCD đã có nhưng SĐT không khớp -> từ chối bằng thông báo chung")
    void dangKy_hoSoCoSanSaiSdt_tuChoiChung() {
        when(taiKhoanRepository.existsByTenDangNhap("tenant99")).thenReturn(false);
        when(khachThueRepository.findByCccd("012345678901"))
                .thenReturn(Optional.of(khach(4, "012345678901", "0987654321")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerTenantAccount(dtoHopLe()));

        assertEquals(TenantPortalService.MSG_KHONG_THE_DANG_KY, ex.getMessage());
        verify(taiKhoanRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Đăng ký: hồ sơ đã có tài khoản -> cùng thông báo chung (không lộ CCCD tồn tại)")
    void dangKy_hoSoDaCoTaiKhoan_tuChoiChung() {
        when(taiKhoanRepository.existsByTenDangNhap("tenant99")).thenReturn(false);
        when(khachThueRepository.findByCccd("012345678901"))
                .thenReturn(Optional.of(khach(4, "012345678901", "0912345678")));
        when(taiKhoanRepository.existsByKhachThueId(4)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerTenantAccount(dtoHopLe()));

        assertEquals(TenantPortalService.MSG_KHONG_THE_DANG_KY, ex.getMessage());
        verify(taiKhoanRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Đăng ký: trùng tên đăng nhập -> từ chối, chưa đụng tới hồ sơ khách")
    void dangKy_trungTenDangNhap_tuChoi() {
        when(taiKhoanRepository.existsByTenDangNhap("tenant99")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerTenantAccount(dtoHopLe()));

        assertEquals(TenantPortalService.MSG_TEN_DANG_NHAP_TRUNG, ex.getMessage());
        verifyNoInteractions(khachThueRepository);
        verify(taiKhoanRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Đăng ký: hai người đăng ký đồng thời (vi phạm UNIQUE) -> báo lỗi thân thiện")
    void dangKy_viPhamRangBuocUnique_baoLoiThanThien() {
        when(taiKhoanRepository.existsByTenDangNhap("tenant99")).thenReturn(false);
        when(khachThueRepository.findByCccd("012345678901")).thenReturn(Optional.empty());
        when(khachThueRepository.saveAndFlush(any(KhachThue.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taiKhoanRepository.saveAndFlush(any(TaiKhoan.class)))
                .thenThrow(new DataIntegrityViolationException("UNIQUE KEY violation"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerTenantAccount(dtoHopLe()));

        assertEquals(TenantPortalService.MSG_TRUNG_DU_LIEU, ex.getMessage());
    }

    @ParameterizedTest(name = "CCCD \"{0}\" không hợp lệ")
    @ValueSource(strings = {"12345678", "1234567890123", "12345678a", "", "  ", "123 456 789"})
    @DisplayName("Validate: CCCD phải gồm 9-12 chữ số")
    void validate_cccdSai(String cccd) {
        TenantRegisterDTO dto = dtoHopLe();
        dto.setCccd(cccd);

        assertTrue(service.validateRegistration(dto).containsKey("cccd"));
        assertThrows(IllegalArgumentException.class, () -> service.registerTenantAccount(dto));
        verifyNoInteractions(taiKhoanRepository, khachThueRepository);
    }

    @ParameterizedTest(name = "CCCD \"{0}\" hợp lệ")
    @ValueSource(strings = {"123456789", "1234567890", "012345678901"})
    @DisplayName("Validate: CCCD 9, 10 và 12 chữ số hợp lệ")
    void validate_cccdDung(String cccd) {
        TenantRegisterDTO dto = dtoHopLe();
        dto.setCccd(cccd);

        assertTrue(service.validateRegistration(dto).isEmpty());
    }

    @ParameterizedTest(name = "SĐT \"{0}\" không hợp lệ")
    @ValueSource(strings = {"123456789", "091234567", "09123456789", "1912345678", "09123abc78", ""})
    @DisplayName("Validate: SĐT phải dạng 0xxxxxxxxx (10 số)")
    void validate_soDienThoaiSai(String sdt) {
        TenantRegisterDTO dto = dtoHopLe();
        dto.setSoDienThoai(sdt);

        assertTrue(service.validateRegistration(dto).containsKey("soDienThoai"));
    }

    @ParameterizedTest(name = "email \"{0}\" không hợp lệ")
    @ValueSource(strings = {"abc", "a@b", "a b@c.com", "@c.com", "abc@.com"})
    @DisplayName("Validate: email nhập vào phải đúng định dạng")
    void validate_emailSai(String email) {
        TenantRegisterDTO dto = dtoHopLe();
        dto.setEmail(email);

        assertTrue(service.validateRegistration(dto).containsKey("email"));
    }

    @Test
    @DisplayName("Validate: email để trống được chấp nhận (không bắt buộc)")
    void validate_emailTrong_duocChapNhan() {
        TenantRegisterDTO dto = dtoHopLe();
        dto.setEmail("  ");

        assertTrue(service.validateRegistration(dto).isEmpty());
    }

    @Test
    @DisplayName("Validate: mật khẩu ngắn hơn 6 ký tự bị từ chối")
    void validate_matKhauNgan() {
        TenantRegisterDTO dto = dtoHopLe();
        dto.setMatKhau("12345");
        dto.setXacNhanMatKhau("12345");

        assertTrue(service.validateRegistration(dto).containsKey("matKhau"));
    }

    @Test
    @DisplayName("Validate: mật khẩu xác nhận không khớp bị từ chối")
    void validate_matKhauKhongKhop() {
        TenantRegisterDTO dto = dtoHopLe();
        dto.setXacNhanMatKhau("Khac123");

        Map<String, String> errors = service.validateRegistration(dto);

        assertTrue(errors.containsKey("xacNhanMatKhau"));
        assertFalse(errors.containsKey("matKhau"));
    }

    @ParameterizedTest(name = "tên đăng nhập \"{0}\" không hợp lệ")
    @ValueSource(strings = {"", "abc", "co dau cach", "tên_có_dấu", "a2345678901234567890123456789012345678901234567890x"})
    @DisplayName("Validate: tên đăng nhập 4-50 ký tự chữ không dấu/số/./_")
    void validate_tenDangNhapSai(String ten) {
        TenantRegisterDTO dto = dtoHopLe();
        dto.setTenDangNhap(ten);

        assertTrue(service.validateRegistration(dto).containsKey("tenDangNhap"));
    }

    @Test
    @DisplayName("Validate: họ tên bắt buộc")
    void validate_hoTenTrong() {
        TenantRegisterDTO dto = dtoHopLe();
        dto.setHoTen("   ");

        assertTrue(service.validateRegistration(dto).containsKey("hoTen"));
    }

    @Test
    @DisplayName("Validate: dữ liệu hợp lệ không có lỗi nào")
    void validate_duLieuHopLe() {
        assertTrue(service.validateRegistration(dtoHopLe()).isEmpty());
    }
}
