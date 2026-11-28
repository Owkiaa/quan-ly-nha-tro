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
import com.nhatro.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Nghiệp vụ Cổng Khách thuê (TV3).
 *
 * <p>Nguyên tắc cô lập dữ liệu: mọi hàm đọc/ghi dữ liệu cá nhân đều nhận {@code khachId} (lấy từ session ở
 * Controller, KHÔNG BAO GIỜ lấy từ tham số request) và đưa thẳng vào điều kiện truy vấn.
 */
@Service
public class TenantPortalService {

    private static final Logger log = LoggerFactory.getLogger(TenantPortalService.class);

    // ------------------------------------------------------------------ Hằng số nghiệp vụ

    public static final int NOI_DUNG_TOI_DA = 255;
    public static final Set<String> MUC_DO_HOP_LE = Set.of("BINH_THUONG", "CAP_BACH");
    public static final String MSG_KHONG_CO_HOP_DONG =
            "Bạn chỉ có thể gửi phản ánh cho phòng đang có hợp đồng còn hiệu lực của mình.";
    public static final String MSG_KHONG_TIM_THAY_KHACH = "Không tìm thấy hồ sơ khách thuê của bạn.";

    /** Số phiếu DANG_GIU tối đa của một khách. */
    public static final int SO_PHONG_GIU_TOI_DA = 2;
    public static final int SO_GIO_GIU_MAC_DINH = 24;
    public static final int SO_GIO_GIU_TOI_THIEU = 1;
    public static final int SO_GIO_GIU_TOI_DA = 72;
    public static final int GHI_CHU_TOI_DA = 255;
    public static final String MSG_KHONG_TIM_THAY_PHIEU = "Không tìm thấy phiếu đặt chỗ của bạn.";

    private static final String DANG_GIU = "DANG_GIU";
    /** Bảng dat_cho chỉ cho 3 trạng thái (DANG_GIU, HET_HAN, DA_CHUYEN_HOP_DONG) nên khách hủy = HET_HAN + ghi chú. */
    private static final String DA_KET_THUC = "HET_HAN";
    private static final DateTimeFormatter GIO_PHUT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static final int MAT_KHAU_TOI_THIEU = 6;
    /** Thông báo chung cho mọi trường hợp CCCD đã có: tránh lộ CCCD nào đã tồn tại trong hệ thống. */
    public static final String MSG_KHONG_THE_DANG_KY =
            "Không thể đăng ký với thông tin này. Nếu bạn đã từng thuê trọ hoặc đã có tài khoản, "
                    + "vui lòng liên hệ quản lý để được hỗ trợ.";
    public static final String MSG_TEN_DANG_NHAP_TRUNG = "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.";
    public static final String MSG_TRUNG_DU_LIEU =
            "Tên đăng nhập hoặc CCCD đã được sử dụng. Vui lòng thử lại hoặc liên hệ quản lý.";

    private static final Pattern CCCD = Pattern.compile("^\\d{9,12}$");
    private static final Pattern SO_DIEN_THOAI = Pattern.compile("^0\\d{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TEN_DANG_NHAP = Pattern.compile("^[A-Za-z0-9._]{4,50}$");

    private static final Set<String> ROOM_SORT_FIELDS = Set.of("giaThue", "dienTich", "tenPhong", "maPhong");
    private static final Set<String> INVOICE_SORT_FIELDS = Set.of("ngayLap", "tongTien");
    private static final int MAX_PAGE_SIZE = 50;
    /** decimal(12,2) tối đa 9.999.999.999,99: dùng làm cận trên khi khách không nhập "giá đến". */
    private static final BigDecimal GIA_TOI_DA = new BigDecimal("9999999999");

    private final PhongTroRepository phongTroRepository;
    private final KhachThueRepository khachThueRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final TenantContractRepository contractRepository;
    private final TenantInvoiceRepository invoiceRepository;
    private final TenantIncidentRepository incidentRepository;
    private final DatChoRepository datChoRepository;
    private final TenantBookingRepository bookingRepository;
    private final DatChoService datChoService;

    public TenantPortalService(PhongTroRepository phongTroRepository,
                               KhachThueRepository khachThueRepository,
                               TaiKhoanRepository taiKhoanRepository,
                               TenantContractRepository contractRepository,
                               TenantInvoiceRepository invoiceRepository,
                               TenantIncidentRepository incidentRepository,
                               DatChoRepository datChoRepository,
                               TenantBookingRepository bookingRepository,
                               DatChoService datChoService) {
        this.phongTroRepository = phongTroRepository;
        this.khachThueRepository = khachThueRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.contractRepository = contractRepository;
        this.invoiceRepository = invoiceRepository;
        this.incidentRepository = incidentRepository;
        this.datChoRepository = datChoRepository;
        this.bookingRepository = bookingRepository;
        this.datChoService = datChoService;
    }

    // ------------------------------------------------------------------ Phòng trống

    @Transactional(readOnly = true)
    public Page<PhongTro> searchAvailableRooms(String keyword, Pageable pageable) {
        return searchAvailableRooms(keyword, null, null, null, null, pageable);
    }

    /**
     * Lọc nâng cao phòng TRONG. Mọi tham số lọc đều có thể null (= không lọc).
     * Quy ước: giaTu &lt; 0 coi như 0; giaDen &lt;= 0 coi như không giới hạn; giaTu &gt; giaDen thì đổi chỗ;
     * dienTichToiThieu âm/NaN coi như 0 (phòng chưa có diện tích được tính là 0 m²).
     */
    @Transactional(readOnly = true)
    public Page<PhongTro> searchAvailableRooms(String keyword, String loaiPhong,
                                               BigDecimal giaTu, BigDecimal giaDen,
                                               Double dienTichToiThieu, Pageable pageable) {
        BigDecimal from = (giaTu == null || giaTu.signum() < 0) ? BigDecimal.ZERO : giaTu;
        BigDecimal to = (giaDen == null || giaDen.signum() <= 0) ? GIA_TOI_DA : giaDen.min(GIA_TOI_DA);
        if (from.compareTo(to) > 0) {
            BigDecimal tmp = from;
            from = to;
            to = tmp;
        }
        double minArea = (dienTichToiThieu == null || dienTichToiThieu.isNaN()
                || dienTichToiThieu.isInfinite() || dienTichToiThieu < 0) ? 0.0 : dienTichToiThieu;

        Pageable safe = sanitize(pageable, ROOM_SORT_FIELDS, Sort.by("giaThue").ascending());
        return phongTroRepository.searchAvailable(blankToNull(keyword), blankToNull(loaiPhong),
                from, to, minArea, safe);
    }

    /** Danh sách loại phòng hiện có, để Controller đưa ra ô chọn lọc. */
    @Transactional(readOnly = true)
    public List<String> getRoomTypes() {
        return phongTroRepository.findDistinctLoaiPhong();
    }

    @Transactional(readOnly = true)
    public Optional<PhongTro> findRoom(Integer id) {
        return phongTroRepository.findById(id);
    }

    // ------------------------------------------------------------------ Hợp đồng

    @Transactional(readOnly = true)
    public List<HopDong> getContracts(Integer khachId) {
        return contractRepository.findAllByKhach(khachId);
    }

    @Transactional(readOnly = true)
    public List<HopDong> getActiveContracts(Integer khachId) {
        return contractRepository.findActiveByKhach(khachId);
    }

    // ------------------------------------------------------------------ Hóa đơn

    @Transactional(readOnly = true)
    public Page<HoaDon> getInvoices(Integer khachId, String trangThai, Pageable pageable) {
        Pageable safe = sanitize(pageable, INVOICE_SORT_FIELDS, Sort.by("ngayLap").descending());
        return invoiceRepository.findByKhach(khachId, blankToNull(trangThai), safe);
    }

    @Transactional(readOnly = true)
    public Optional<HoaDon> findInvoice(Integer khachId, Integer invoiceId) {
        return invoiceRepository.findByIdAndKhach(invoiceId, khachId);
    }

    @Transactional(readOnly = true)
    public long countUnpaidInvoices(Integer khachId) {
        return invoiceRepository.countUnpaid(khachId);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumUnpaidInvoices(Integer khachId) {
        BigDecimal sum = invoiceRepository.sumUnpaid(khachId);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    // ------------------------------------------------------------------ Sự cố

    @Transactional(readOnly = true)
    public List<YeuCauSuaChua> getIncidents(Integer khachId) {
        return incidentRepository.findAllByKhach(khachId);
    }

    /**
     * Tạo phiếu phản ánh. Chỉ cho phép với phòng khách đang có hợp đồng HIEU_LUC.
     *
     * @throws IllegalArgumentException thông báo tiếng Việt, hiển thị thẳng cho người dùng
     */
    @Transactional
    public YeuCauSuaChua createIncident(Integer khachId, Integer phongId, String noiDung, String mucDo) {
        KhachThue khach = khachThueRepository.findById(khachId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_KHONG_TIM_THAY_KHACH));

        List<HopDong> contracts = contractRepository.findActiveByKhachAndPhong(khachId, phongId);
        if (contracts.isEmpty()) {
            throw new IllegalArgumentException(MSG_KHONG_CO_HOP_DONG);
        }
        PhongTro phong = contracts.get(0).getPhongTro();

        YeuCauSuaChua yc = new YeuCauSuaChua();
        yc.setPhongTro(phong);
        yc.setKhachThue(khach);
        yc.setNoiDung(noiDung.trim());
        yc.setMucDo(mucDo);
        yc.setTrangThai("CHO_TIEP_NHAN");
        yc.setChiPhi(BigDecimal.ZERO);
        yc.setNgayTao(LocalDateTime.now());
        return incidentRepository.save(yc);
    }

    // ------------------------------------------------------------------ Đặt giữ chỗ trực tuyến

    /**
     * Khách tự đặt giữ chỗ một phòng TRONG. Tối đa {@value #SO_PHONG_GIU_TOI_DA} phiếu DANG_GIU mỗi khách.
     * Việc khóa phòng (bi quan) và đổi trạng thái phòng do {@code DatChoService.datChoPhong} của TV2 đảm nhiệm.
     *
     * @param soGioGiuCho null = {@value #SO_GIO_GIU_MAC_DINH} giờ; hợp lệ từ 1 đến 72 giờ
     * @throws IllegalArgumentException dữ liệu không hợp lệ hoặc vượt hạn mức
     * @throws IllegalStateException    phòng không thể giữ chỗ lúc này (đã có người giữ, đã thuê...)
     */
    @Transactional
    public DatCho bookRoomForTenant(Integer khachId, Integer phongId, Integer soGioGiuCho, String ghiChu) {
        if (khachId == null || phongId == null) {
            throw new IllegalArgumentException("Thiếu thông tin khách thuê hoặc phòng cần giữ chỗ.");
        }
        int soGio = (soGioGiuCho == null) ? SO_GIO_GIU_MAC_DINH : soGioGiuCho;
        if (soGio < SO_GIO_GIU_TOI_THIEU || soGio > SO_GIO_GIU_TOI_DA) {
            throw new IllegalArgumentException("Thời gian giữ chỗ phải từ " + SO_GIO_GIU_TOI_THIEU
                    + " đến " + SO_GIO_GIU_TOI_DA + " giờ.");
        }
        String note = blankToNull(ghiChu);
        if (note != null && note.length() > GHI_CHU_TOI_DA) {
            throw new IllegalArgumentException("Ghi chú tối đa " + GHI_CHU_TOI_DA + " ký tự.");
        }
        if (!khachThueRepository.existsById(khachId)) {
            throw new IllegalArgumentException(MSG_KHONG_TIM_THAY_KHACH);
        }

        long dangGiu = bookingRepository.countByKhachThueIdAndTrangThai(khachId, DANG_GIU);
        if (dangGiu >= SO_PHONG_GIU_TOI_DA) {
            throw new IllegalArgumentException("Bạn đang giữ chỗ tối đa " + SO_PHONG_GIU_TOI_DA
                    + " phòng. Hãy hủy một phiếu hoặc chờ phiếu hết hạn rồi đặt tiếp.");
        }

        try {
            return goiDatChoService(khachId, phongId, soGio, note);
        } catch (RuntimeException ex) {
            if (laLoiNghiepVu(ex)) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
            log.warn("Đặt giữ chỗ thất bại (khachId={}, phongId={})", khachId, phongId, ex);
            throw new IllegalStateException("Không thể giữ chỗ phòng này lúc này. Vui lòng thử lại sau.", ex);
        }
    }

    /**
     * >>> ĐIỂM GHÉP DUY NHẤT với DatChoService của TV2 (khóa bi quan findByIdForUpdate nằm trong đó).
     * Giả định chữ ký: DatCho datChoPhong(Integer khachId, Integer phongId, int soGioGiuCho, String ghiChu).
     * Nếu chữ ký thật khác, chỉ cần sửa đúng dòng gọi bên dưới.
     */
    private DatCho goiDatChoService(Integer khachId, Integer phongId, int soGio, String ghiChu) {
        return datChoService.datChoPhong(khachId, phongId, soGio, ghiChu);
    }

    /** Lỗi nghiệp vụ (thông báo an toàn để hiện cho khách) khác lỗi hạ tầng (SQL, khóa...) cần ẩn chi tiết. */
    private static boolean laLoiNghiepVu(RuntimeException ex) {
        return ex.getMessage() != null
                && (ex instanceof IllegalArgumentException
                || ex instanceof IllegalStateException
                || ex.getClass().getName().startsWith("com.nhatro."));
    }

    @Transactional(readOnly = true)
    public List<DatCho> getBookingsByKhach(Integer khachId) {
        return bookingRepository.findAllByKhach(khachId);
    }

    /**
     * Khách hủy phiếu giữ chỗ CỦA CHÍNH MÌNH. Chống IDOR: phiếu của người khác và phiếu không tồn tại cho
     * cùng một thông báo, truy vấn luôn ràng buộc khachId.
     *
     * <p>Thứ tự khóa: phòng trước, phiếu sau (giống DatChoService) để không bị khóa chéo.
     */
    @Transactional
    public void cancelBookingByKhach(Integer khachId, Integer bookingId) {
        if (khachId == null || bookingId == null) {
            throw new IllegalArgumentException(MSG_KHONG_TIM_THAY_PHIEU);
        }
        // Chỉ lấy id phòng (không nạp entity phiếu) để lần nạp phiếu bên dưới đọc trạng thái mới nhất sau khi khóa phòng.
        Integer phongId = bookingRepository.findPhongIdByIdAndKhach(bookingId, khachId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_KHONG_TIM_THAY_PHIEU));
        PhongTro phong = phongTroRepository.findByIdForUpdate(phongId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_KHONG_TIM_THAY_PHIEU));
        DatCho datCho = bookingRepository.findByIdAndKhach(bookingId, khachId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_KHONG_TIM_THAY_PHIEU));

        if (!DANG_GIU.equals(datCho.getTrangThai())) {
            throw new IllegalStateException("Phiếu đặt chỗ này không còn ở trạng thái đang giữ nên không thể hủy.");
        }

        datCho.setTrangThai(DA_KET_THUC);
        datCho.setGhiChu(noiGhiChu(datCho.getGhiChu(),
                "Khách tự hủy lúc " + LocalDateTime.now().format(GIO_PHUT)));
        bookingRepository.save(datCho);

        // Chỉ trả phòng về TRONG khi không còn phiếu DANG_GIU nào khác giữ phòng này.
        if ("DA_DAT".equals(phong.getTrangThai())
                && !datChoRepository.existsByPhongTroIdAndTrangThai(phongId, DANG_GIU)) {
            phong.setTrangThai("TRONG");
            phongTroRepository.save(phong);
        }
    }

    private static String noiGhiChu(String cu, String them) {
        if (cu == null || cu.isBlank()) {
            return cat(them);
        }
        String sep = " | ";
        int conLai = GHI_CHU_TOI_DA - them.length() - sep.length();
        if (conLai <= 0) {
            return cat(them);
        }
        String dau = cu.length() > conLai ? cu.substring(0, conLai) : cu;
        return dau + sep + them;
    }

    private static String cat(String s) {
        return s.length() > GHI_CHU_TOI_DA ? s.substring(0, GHI_CHU_TOI_DA) : s;
    }

    // ------------------------------------------------------------------ Tự đăng ký tài khoản khách thuê

    /**
     * Kiểm tra dữ liệu đăng ký. Trả về map tên trường (tenDangNhap, matKhau, xacNhanMatKhau, hoTen, cccd,
     * soDienThoai, email) -> thông báo lỗi; rỗng nghĩa là hợp lệ. Không truy cập CSDL.
     */
    public Map<String, String> validateRegistration(TenantRegisterDTO dto) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (dto == null) {
            errors.put("tenDangNhap", "Thiếu thông tin đăng ký.");
            return errors;
        }
        String tenDangNhap = trim(dto.getTenDangNhap());
        if (tenDangNhap.isEmpty()) {
            errors.put("tenDangNhap", "Vui lòng nhập tên đăng nhập.");
        } else if (!TEN_DANG_NHAP.matcher(tenDangNhap).matches()) {
            errors.put("tenDangNhap", "Tên đăng nhập gồm 4-50 ký tự chữ không dấu, số, dấu chấm hoặc gạch dưới.");
        }

        String hoTen = trim(dto.getHoTen());
        if (hoTen.isEmpty()) {
            errors.put("hoTen", "Vui lòng nhập họ tên.");
        } else if (hoTen.length() > 100) {
            errors.put("hoTen", "Họ tên tối đa 100 ký tự.");
        }

        if (!CCCD.matcher(trim(dto.getCccd())).matches()) {
            errors.put("cccd", "CCCD phải gồm từ 9 đến 12 chữ số.");
        }
        if (!SO_DIEN_THOAI.matcher(trim(dto.getSoDienThoai())).matches()) {
            errors.put("soDienThoai", "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");
        }

        String email = trim(dto.getEmail());
        if (!email.isEmpty() && (email.length() > 100 || !EMAIL.matcher(email).matches())) {
            errors.put("email", "Email không đúng định dạng.");
        }

        String matKhau = dto.getMatKhau();
        if (matKhau == null || matKhau.length() < MAT_KHAU_TOI_THIEU) {
            errors.put("matKhau", "Mật khẩu phải có ít nhất " + MAT_KHAU_TOI_THIEU + " ký tự.");
        } else if (!matKhau.equals(dto.getXacNhanMatKhau())) {
            errors.put("xacNhanMatKhau", "Mật khẩu xác nhận không khớp.");
        }
        return errors;
    }

    /**
     * Khách tự đăng ký tài khoản (vai trò TENANT). Toàn bộ trong một giao dịch: lỗi ở bước nào cũng hoàn tác hết.
     *
     * <ul>
     *   <li>CCCD chưa có trong hệ thống: tạo hồ sơ KhachThue mới (trạng thái DA_TRA_PHONG = chưa thuê phòng nào).</li>
     *   <li>CCCD đã có: chỉ liên kết khi hồ sơ CHƯA có tài khoản VÀ số điện thoại khớp hồ sơ. Mọi trường hợp
     *       còn lại trả cùng một thông báo chung, tránh người lạ dùng CCCD của người khác để xem hợp đồng, hóa đơn.</li>
     * </ul>
     *
     * @throws IllegalArgumentException thông báo tiếng Việt, hiển thị thẳng cho người dùng
     */
    @Transactional
    public TaiKhoan registerTenantAccount(TenantRegisterDTO dto) {
        Map<String, String> errors = validateRegistration(dto);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.values().iterator().next());
        }
        String tenDangNhap = trim(dto.getTenDangNhap());
        String cccd = trim(dto.getCccd());
        String soDienThoai = trim(dto.getSoDienThoai());

        if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
            throw new IllegalArgumentException(MSG_TEN_DANG_NHAP_TRUNG);
        }

        try {
            KhachThue khach;
            Optional<KhachThue> daCo = khachThueRepository.findByCccd(cccd);
            if (daCo.isPresent()) {
                KhachThue hoSo = daCo.get();
                boolean sdtKhop = soDienThoai.equals(chuanHoaSdt(hoSo.getSoDienThoai()));
                boolean daCoTaiKhoan = taiKhoanRepository.existsByKhachThueId(hoSo.getId());
                if (!sdtKhop || daCoTaiKhoan) {
                    throw new IllegalArgumentException(MSG_KHONG_THE_DANG_KY);
                }
                khach = hoSo;
            } else {
                String email = blankToNull(dto.getEmail());
                khach = khachThueRepository.saveAndFlush(new KhachThue(trim(dto.getHoTen()), cccd,
                        soDienThoai, email, null, null, null, null, "DA_TRA_PHONG"));
            }

            TaiKhoan taiKhoan = new TaiKhoan(tenDangNhap, PasswordUtil.hash(dto.getMatKhau()), "TENANT",
                    khach, true, LocalDateTime.now());
            return taiKhoanRepository.saveAndFlush(taiKhoan);
        } catch (DataIntegrityViolationException ex) {
            // Hai người đăng ký cùng lúc cùng tên đăng nhập / CCCD: ràng buộc UNIQUE của CSDL là chốt chặn cuối.
            throw new IllegalArgumentException(MSG_TRUNG_DU_LIEU, ex);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String chuanHoaSdt(String s) {
        return s == null ? "" : s.replaceAll("[\\s.\\-]", "");
    }

    // ------------------------------------------------------------------ Tiện ích

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /** Chỉ giữ các trường sắp xếp hợp lệ (tránh lỗi 500 khi ai đó sửa URL) và giới hạn cỡ trang. */
    private static Pageable sanitize(Pageable pageable, Set<String> allowed, Sort defaultSort) {
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if (allowed.contains(order.getProperty())) {
                orders.add(order);
            }
        }
        Sort sort = orders.isEmpty() ? defaultSort : Sort.by(orders);
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(pageable.getPageNumber(), 0), size, sort);
    }
}
