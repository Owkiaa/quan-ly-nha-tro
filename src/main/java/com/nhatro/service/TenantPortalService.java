package com.nhatro.service;

import com.nhatro.entity.HoaDon;
import com.nhatro.entity.HopDong;
import com.nhatro.entity.KhachThue;
import com.nhatro.entity.PhongTro;
import com.nhatro.entity.YeuCauSuaChua;
import com.nhatro.repository.KhachThueRepository;
import com.nhatro.repository.PhongTroRepository;
import com.nhatro.repository.TenantContractRepository;
import com.nhatro.repository.TenantIncidentRepository;
import com.nhatro.repository.TenantInvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Nghiệp vụ Cổng Khách thuê (TV3).
 * Mọi hàm dữ liệu cá nhân đều nhận khachId và truy vấn ràng buộc theo khachId đó.
 */
@Service
public class TenantPortalService {

    public static final int NOI_DUNG_TOI_DA = 255;
    public static final Set<String> MUC_DO_HOP_LE = Set.of("BINH_THUONG", "CAP_BACH");

    private static final Set<String> ROOM_SORT_FIELDS = Set.of("giaThue", "dienTich", "tenPhong", "maPhong");
    private static final Set<String> INVOICE_SORT_FIELDS = Set.of("ngayLap", "tongTien");
    private static final int MAX_PAGE_SIZE = 50;

    private final PhongTroRepository phongTroRepository;
    private final KhachThueRepository khachThueRepository;
    private final TenantContractRepository contractRepository;
    private final TenantInvoiceRepository invoiceRepository;
    private final TenantIncidentRepository incidentRepository;

    public TenantPortalService(PhongTroRepository phongTroRepository,
                               KhachThueRepository khachThueRepository,
                               TenantContractRepository contractRepository,
                               TenantInvoiceRepository invoiceRepository,
                               TenantIncidentRepository incidentRepository) {
        this.phongTroRepository = phongTroRepository;
        this.khachThueRepository = khachThueRepository;
        this.contractRepository = contractRepository;
        this.invoiceRepository = invoiceRepository;
        this.incidentRepository = incidentRepository;
    }

    // ---------------------------------------------------------------- Phòng trống

    @Transactional(readOnly = true)
    public Page<PhongTro> searchAvailableRooms(String keyword, Pageable pageable) {
        Pageable safe = sanitize(pageable, ROOM_SORT_FIELDS, Sort.by("giaThue").ascending());
        return phongTroRepository.search(blankToNull(keyword), "TRONG", safe);
    }

    @Transactional(readOnly = true)
    public Optional<PhongTro> findRoom(Integer id) {
        return phongTroRepository.findById(id);
    }

    // ---------------------------------------------------------------- Hợp đồng

    @Transactional(readOnly = true)
    public List<HopDong> getContracts(Integer khachId) {
        return contractRepository.findAllByKhach(khachId);
    }

    @Transactional(readOnly = true)
    public List<HopDong> getActiveContracts(Integer khachId) {
        return contractRepository.findActiveByKhach(khachId);
    }

    // ---------------------------------------------------------------- Hóa đơn

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

    // ---------------------------------------------------------------- Sự cố

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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ khách thuê của bạn."));

        List<HopDong> contracts = contractRepository.findActiveByKhachAndPhong(khachId, phongId);
        if (contracts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Bạn chỉ có thể gửi phản ánh cho phòng đang có hợp đồng còn hiệu lực của mình.");
        }

        YeuCauSuaChua yc = new YeuCauSuaChua();
        yc.setPhongTro(contracts.get(0).getPhongTro());
        yc.setKhachThue(khach);
        yc.setNoiDung(noiDung.trim());
        yc.setMucDo(mucDo);
        yc.setTrangThai("CHO_TIEP_NHAN");
        yc.setChiPhi(BigDecimal.ZERO);
        yc.setNgayTao(LocalDateTime.now());
        return incidentRepository.save(yc);
    }

    // ---------------------------------------------------------------- Tiện ích

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
