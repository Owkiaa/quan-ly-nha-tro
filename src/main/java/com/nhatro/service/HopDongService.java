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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý hợp đồng thuê phòng và các ràng buộc toàn vẹn nghiệp vụ.
 * Phụ trách bởi: Bùi Trần Tấn Tài
 */
@Service
public class HopDongService {

    private static final Logger log = LoggerFactory.getLogger(HopDongService.class);

    public static final String TRANG_THAI_HIEU_LUC = "HIEU_LUC";
    public static final String TRANG_THAI_HET_HAN = "HET_HAN";
    public static final String TRANG_THAI_DA_THANH_LY = "DA_THANH_LY";

    public static final String PHONG_TRONG = "TRONG";
    public static final String PHONG_DA_DAT = "DA_DAT";
    public static final String PHONG_DA_THUE = "DA_THUE";

    public static final String DAT_CHO_DANG_GIU = "DANG_GIU";
    public static final String DAT_CHO_DA_CHUYEN_HOP_DONG = "DA_CHUYEN_HOP_DONG";

    @Autowired
    private HopDongRepository hopDongRepository;

    @Autowired
    private PhongTroRepository phongTroRepository;

    @Autowired
    private KhachThueRepository khachThueRepository;

    @Autowired
    private DatChoRepository datChoRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    /**
     * Tạo mới hợp đồng thuê phòng với các ràng buộc toàn vẹn nghiêm ngặt:
     * 1. Khóa dòng phòng trọ (Pessimistic Lock) chống tranh chấp.
     * 2. Chỉ cho phép lập hợp đồng với phòng TRONG hoặc phòng DA_DAT nếu đúng khách giữ chỗ.
     * 3. Chuyển trạng thái bản ghi dat_cho sang DA_CHUYEN_HOP_DONG và phòng sang DA_THUE.
     */
    @Transactional
    public HopDong taoHopDong(HopDongRequestDTO request) {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Bắt đầu xử lý tạo hợp đồng cho phòng ID: {}, khách ID: {}",
                threadName, request.getPhongId(), request.getKhachId());

        // Validate cơ bản
        if (request.getPhongId() == null) {
            throw new BusinessException("Vui lòng chọn phòng trọ cho hợp đồng.");
        }
        if (request.getKhachId() == null) {
            throw new BusinessException("Vui lòng chọn khách thuê cho hợp đồng.");
        }
        if (request.getNgayBatDau() == null) {
            throw new BusinessException("Ngày bắt đầu hợp đồng không được để trống.");
        }
        if (request.getNgayKetThuc() == null) {
            throw new BusinessException("Ngày kết thúc hợp đồng không được để trống.");
        }
        if (!request.getNgayKetThuc().isAfter(request.getNgayBatDau())) {
            throw new BusinessException("Ngày kết thúc hợp đồng phải sau ngày bắt đầu.");
        }
        if (request.getTienDatCoc() == null || request.getTienDatCoc().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Tiền đặt cọc không hợp lệ (phải >= 0).");
        }
        if (request.getGiaThueThoaThuan() == null || request.getGiaThueThoaThuan().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Giá thuê thỏa thuận không hợp lệ (phải > 0).");
        }

        // 1. Khóa dòng dữ liệu phòng trọ bằng Pessimistic Lock
        PhongTro phong = phongTroRepository.findByIdForUpdate(request.getPhongId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy phòng trọ ID: " + request.getPhongId()));

        // 2. Tìm thông tin khách thuê
        KhachThue khach = khachThueRepository.findById(request.getKhachId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy khách thuê ID: " + request.getKhachId()));

        // 3. Kiểm tra mã hợp đồng (tự động sinh nếu chưa nhập)
        String maHopDong = request.getMaHopDong();
        if (maHopDong == null || maHopDong.trim().isEmpty()) {
            maHopDong = sinhMaHopDongTuDong(phong.getMaPhong());
        } else {
            maHopDong = maHopDong.trim();
            if (hopDongRepository.existsByMaHopDong(maHopDong)) {
                throw new BusinessException("Mã hợp đồng '" + maHopDong + "' đã tồn tại trong hệ thống.");
            }
        }

        // 4. Kiểm tra trạng thái phòng và quyền ký hợp đồng
        if (PHONG_TRONG.equalsIgnoreCase(phong.getTrangThai())) {
            // Phòng trống: Khách có thể ký hợp đồng trực tiếp
            log.info("[{}] Phòng {} đang TRONG, tiến hành lập hợp đồng trực tiếp", threadName, phong.getMaPhong());
        } else if (PHONG_DA_DAT.equalsIgnoreCase(phong.getTrangThai())) {
            // Phòng đã đặt cọc / giữ chỗ: Kiểm tra xem người ký có đúng là người đã giữ chỗ không
            Optional<DatCho> optDatCho = datChoRepository.findFirstByPhongTroIdAndTrangThai(phong.getId(), DAT_CHO_DANG_GIU);
            if (optDatCho.isPresent()) {
                DatCho datCho = optDatCho.get();
                if (!datCho.getKhachThue().getId().equals(khach.getId())) {
                    log.warn("[{}] Phòng {} đang giữ chỗ bởi khách ID: {} (CCCD: {}), nhưng khách ID: {} (CCCD: {}) cố ký hợp đồng",
                            threadName, phong.getMaPhong(), datCho.getKhachThue().getId(), datCho.getKhachThue().getCccd(),
                            khach.getId(), khach.getCccd());
                    throw new BusinessException("Phòng " + phong.getMaPhong() + " đang được giữ chỗ bởi khách '"
                            + datCho.getKhachThue().getHoTen() + "' (CCCD: " + datCho.getKhachThue().getCccd()
                            + "). Chỉ khách đã giữ chỗ mới được phép ký hợp đồng.");
                }
                // Đúng khách đã giữ chỗ -> chuyển đặt chỗ sang DA_CHUYEN_HOP_DONG
                datCho.setTrangThai(DAT_CHO_DA_CHUYEN_HOP_DONG);
                datChoRepository.save(datCho);
                log.info("[{}] Đã chuyển trạng thái đặt chỗ ID: {} sang DA_CHUYEN_HOP_DONG", threadName, datCho.getId());
            } else {
                throw new BusinessException("Phòng " + phong.getMaPhong() + " ở trạng thái ĐÃ ĐẶT nhưng không tìm thấy thông tin đặt giữ chỗ hợp lệ.");
            }
        } else {
            // Phòng đang thuê hoặc bảo trì: Không được phép ký
            throw new BusinessException("Không thể lập hợp đồng cho phòng " + phong.getMaPhong()
                    + " vì trạng thái hiện tại là: " + phong.getTrangThai() + ".");
        }

        // 5. Cập nhật trạng thái phòng sang DA_THUE
        phong.setTrangThai(PHONG_DA_THUE);
        phongTroRepository.save(phong);

        // 6. Cập nhật trạng thái khách thuê sang DANG_THUE
        khach.setTrangThai("DANG_THUE");
        khachThueRepository.save(khach);

        // 7. Tạo bản ghi hợp đồng mới
        HopDong hopDong = new HopDong();
        hopDong.setMaHopDong(maHopDong);
        hopDong.setPhongTro(phong);
        hopDong.setKhachThue(khach);
        hopDong.setNgayBatDau(request.getNgayBatDau());
        hopDong.setNgayKetThuc(request.getNgayKetThuc());
        hopDong.setTienDatCoc(request.getTienDatCoc());
        hopDong.setGiaThueThoaThuan(request.getGiaThueThoaThuan());
        hopDong.setChuKyThanhToan(request.getChuKyThanhToan() != null && request.getChuKyThanhToan() > 0 ? request.getChuKyThanhToan() : 1);
        hopDong.setTrangThai(TRANG_THAI_HIEU_LUC);

        HopDong saved = hopDongRepository.save(hopDong);
        log.info("[{}] Lập hợp đồng THÀNH CÔNG: mã {}, phòng {}, khách {}, hiệu lực đến {}",
                threadName, saved.getMaHopDong(), phong.getMaPhong(), khach.getHoTen(), saved.getNgayKetThuc());

        return saved;
    }

    /**
     * Gia hạn thời hạn hợp đồng.
     */
    @Transactional
    public HopDong giaHanHopDong(Integer hopDongId, LocalDate ngayKetThucMoi) {
        String threadName = Thread.currentThread().getName();
        if (hopDongId == null) {
            throw new BusinessException("Mã ID hợp đồng không được để trống.");
        }
        if (ngayKetThucMoi == null) {
            throw new BusinessException("Ngày kết thúc mới không được để trống.");
        }

        HopDong hopDong = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hợp đồng ID: " + hopDongId));

        if (TRANG_THAI_DA_THANH_LY.equals(hopDong.getTrangThai())) {
            throw new BusinessException("Không thể gia hạn hợp đồng đã thanh lý.");
        }

        if (!ngayKetThucMoi.isAfter(hopDong.getNgayKetThuc())) {
            throw new BusinessException("Ngày kết thúc mới (" + ngayKetThucMoi
                    + ") phải sau ngày kết thúc hiện tại (" + hopDong.getNgayKetThuc() + ").");
        }

        LocalDate ngayCu = hopDong.getNgayKetThuc();
        hopDong.setNgayKetThuc(ngayKetThucMoi);
        hopDong.setTrangThai(TRANG_THAI_HIEU_LUC);
        HopDong saved = hopDongRepository.save(hopDong);

        log.info("[{}] Gia hạn thành công hợp đồng {}: từ ngày {} sang ngày {}",
                threadName, saved.getMaHopDong(), ngayCu, ngayKetThucMoi);

        return saved;
    }

    /**
     * Thanh lý hợp đồng thuê phòng và tự động trả phòng về trạng thái TRONG.
     */
    @Transactional
    public HopDong thanhLyHopDong(Integer hopDongId) {
        String threadName = Thread.currentThread().getName();
        if (hopDongId == null) {
            throw new BusinessException("Mã ID hợp đồng không được để trống.");
        }

        HopDong hopDong = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hợp đồng ID: " + hopDongId));

        if (TRANG_THAI_DA_THANH_LY.equals(hopDong.getTrangThai())) {
            log.info("[{}] Hợp đồng {} đã ở trạng thái thanh lý từ trước", threadName, hopDong.getMaHopDong());
            return hopDong;
        }

        // 1. Khóa phòng trọ để cập nhật trạng thái
        PhongTro phong = phongTroRepository.findByIdForUpdate(hopDong.getPhongTro().getId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy phòng trọ liên quan."));

        // 2. Chuyển trạng thái hợp đồng sang DA_THANH_LY
        hopDong.setTrangThai(TRANG_THAI_DA_THANH_LY);
        HopDong saved = hopDongRepository.save(hopDong);

        // 3. Trả phòng về TRONG nếu phòng đang DA_THUE
        if (PHONG_DA_THUE.equals(phong.getTrangThai())) {
            phong.setTrangThai(PHONG_TRONG);
            phongTroRepository.save(phong);
            log.info("[{}] Đã trả phòng {} về trạng thái TRONG sau khi thanh lý hợp đồng {}",
                    threadName, phong.getMaPhong(), hopDong.getMaHopDong());
        }

        // 4. Kiểm tra khách thuê: nếu không còn hợp đồng hiệu lực nào khác thì chuyển sang DA_TRA_PHONG
        Integer khachId = hopDong.getKhachThue().getId();
        boolean conHopDongHieuLuc = hopDongRepository.existsByKhachThueIdAndTrangThai(khachId, TRANG_THAI_HIEU_LUC);
        if (!conHopDongHieuLuc) {
            KhachThue khach = hopDong.getKhachThue();
            khach.setTrangThai("DA_TRA_PHONG");
            khachThueRepository.save(khach);
            log.info("[{}] Khách thuê {} không còn hợp đồng hiệu lực khác, chuyển sang DA_TRA_PHONG",
                    threadName, khach.getHoTen());
        }

        return saved;
    }

    /**
     * Ràng buộc toàn vẹn khi xóa dữ liệu (Delete Integrity Rule):
     * Không cho phép xóa hợp đồng nếu đã phát sinh hóa đơn tiền phòng (hoa_don).
     */
    @Transactional
    public void xoaHopDong(Integer hopDongId) {
        String threadName = Thread.currentThread().getName();
        if (hopDongId == null) {
            throw new BusinessException("Mã ID hợp đồng không được để trống.");
        }

        HopDong hopDong = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hợp đồng ID: " + hopDongId));

        // RÀNG BUỘC TOÀN VẸN: Chặn xóa nếu đã có hóa đơn
        if (hoaDonRepository.existsByHopDongId(hopDongId)) {
            throw new BusinessException("Không thể xóa hợp đồng đã phát sinh hóa đơn thu tiền. Vui lòng chuyển trạng thái sang Đã thanh lý.");
        }

        // Nếu hợp đồng đang hiệu lực, cần giải phóng phòng về TRONG
        if (TRANG_THAI_HIEU_LUC.equals(hopDong.getTrangThai())) {
            PhongTro phong = phongTroRepository.findByIdForUpdate(hopDong.getPhongTro().getId()).orElse(null);
            if (phong != null && PHONG_DA_THUE.equals(phong.getTrangThai())) {
                phong.setTrangThai(PHONG_TRONG);
                phongTroRepository.save(phong);
            }
        }

        hopDongRepository.delete(hopDong);
        log.info("[{}] Đã xóa thành công hợp đồng ID: {}, mã: {}", threadName, hopDongId, hopDong.getMaHopDong());
    }

    /**
     * Tìm kiếm và phân trang hợp đồng theo từ khóa và trạng thái.
     */
    public Page<HopDong> timKiem(String keyword, String trangThai, Pageable pageable) {
        return hopDongRepository.search(keyword, trangThai, pageable);
    }

    /**
     * Tìm hợp đồng theo ID.
     */
    public Optional<HopDong> timTheoId(Integer id) {
        return hopDongRepository.findById(id);
    }

    /**
     * Tìm hợp đồng theo mã hợp đồng.
     */
    public Optional<HopDong> timTheoMaHopDong(String maHopDong) {
        return hopDongRepository.findByMaHopDong(maHopDong);
    }

    /**
     * Lấy toàn bộ danh sách hợp đồng.
     */
    public List<HopDong> layTatCa() {
        return hopDongRepository.findAll();
    }

    /**
     * Tìm hợp đồng theo khách thuê.
     */
    public List<HopDong> timTheoKhachThue(Integer khachId) {
        return hopDongRepository.findByKhachThueId(khachId);
    }

    /**
     * Tìm hợp đồng theo phòng trọ.
     */
    public List<HopDong> timTheoPhongTro(Integer phongId) {
        return hopDongRepository.findByPhongTroId(phongId);
    }

    /**
     * Sinh mã hợp đồng tự động theo định dạng HD-{maPhong}-{yyMMdd}-{random3Digits}.
     * Ví dụ: HD-P201-261118-123
     */
    public String sinhMaHopDongTuDong(String maPhong) {
        String cleanMaPhong = (maPhong != null ? maPhong.trim().toUpperCase() : "PHONG");
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String maSinh;
        int attempts = 0;
        do {
            int randomNum = (int) (Math.random() * 900) + 100;
            maSinh = "HD-" + cleanMaPhong + "-" + datePart + "-" + randomNum;
            attempts++;
        } while (hopDongRepository.existsByMaHopDong(maSinh) && attempts < 20);

        return maSinh;
    }

    /**
     * Lấy danh sách các phòng có thể lập hợp đồng (trạng thái TRONG hoặc DA_DAT).
     */
    public List<PhongTro> layDanhSachPhongCoTheLapHopDong() {
        return phongTroRepository.findByTrangThaiInOrderByMaPhongAsc(List.of(PHONG_TRONG, PHONG_DA_DAT));
    }

    /**
     * Lấy toàn bộ danh sách khách thuê sắp xếp theo tên.
     */
    public List<KhachThue> layDanhSachKhachThue() {
        return khachThueRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "hoTen"));
    }

    /**
     * Tìm phiếu đặt giữ chỗ đang có hiệu lực của phòng (nếu có).
     */
    public Optional<DatCho> timDatChoDangGiuCuaPhong(Integer phongId) {
        return datChoRepository.findFirstByPhongTroIdAndTrangThai(phongId, DAT_CHO_DANG_GIU);
    }

    public long demHieuLuc() {
        return hopDongRepository.countByTrangThai(TRANG_THAI_HIEU_LUC);
    }

    public long demHetHan() {
        return hopDongRepository.countByTrangThai(TRANG_THAI_HET_HAN);
    }

    public long demDaThanhLy() {
        return hopDongRepository.countByTrangThai(TRANG_THAI_DA_THANH_LY);
    }

    /**
     * Tính toán chuỗi băm SHA-256 toàn vẹn cho hợp đồng.
     */
    public String tinhMaBamToanVen(HopDong hopDong) {
        return com.nhatro.util.ContractHashUtil.generateContractHash(hopDong);
    }

    /**
     * Đối soát tính toàn vẹn của hợp đồng theo ID và mã băm kỳ vọng.
     */
    public boolean kiemTraToanVen(Integer hopDongId, String expectedHash) {
        return hopDongRepository.findById(hopDongId)
                .map(hd -> com.nhatro.util.ContractHashUtil.verifyContractHash(hd, expectedHash))
                .orElse(false);
    }

    /**
     * Lấy danh sách hóa đơn chưa thanh toán của hợp đồng.
     */
    public List<HoaDon> layHoaDonChuaThanhToan(Integer hopDongId) {
        return hoaDonRepository.findByHopDongIdAndTrangThai(hopDongId, "CHUA_THANH_TOAN");
    }

    /**
     * Tính toán bảng kê quyết toán tiền cọc dự kiến trước khi thanh lý hợp đồng.
     */
    public QuyetToanHopDongDTO tinhQuyetToanHopDong(Integer hopDongId) {
        HopDong hopDong = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hợp đồng ID: " + hopDongId));

        List<HoaDon> hoaDonNo = layHoaDonChuaThanhToan(hopDongId);
        BigDecimal tongNo = hoaDonNo.stream()
                .map(hd -> hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tienCoc = hopDong.getTienDatCoc() != null ? hopDong.getTienDatCoc() : BigDecimal.ZERO;
        BigDecimal thucHoan = tienCoc.subtract(tongNo);

        return new QuyetToanHopDongDTO(
                hopDong.getId(),
                hopDong.getMaHopDong(),
                hopDong.getPhongTro().getMaPhong(),
                hopDong.getKhachThue().getHoTen(),
                tienCoc,
                tongNo,
                hoaDonNo.size(),
                BigDecimal.ZERO,
                null,
                thucHoan,
                Boolean.TRUE,
                LocalDate.now()
        );
    }

    /**
     * Thanh lý hợp đồng kèm quyết toán tiền cọc minh bạch (tự động trừ công nợ hóa đơn & khấu trừ hư hại).
     */
    @Transactional
    public QuyetToanHopDongDTO thanhLyKemQuyetToan(Integer hopDongId, QuyetToanHopDongDTO request) {
        String threadName = Thread.currentThread().getName();
        HopDong hopDong = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hợp đồng ID: " + hopDongId));

        if (TRANG_THAI_DA_THANH_LY.equals(hopDong.getTrangThai())) {
            throw new BusinessException("Hợp đồng " + hopDong.getMaHopDong() + " đã được thanh lý trước đó.");
        }

        BigDecimal chiPhiKhauTru = (request != null && request.getChiPhiKhauTru() != null)
                ? request.getChiPhiKhauTru() : BigDecimal.ZERO;
        if (chiPhiKhauTru.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Chi phí khấu trừ không được nhỏ hơn 0.");
        }

        boolean gachNo = request == null || Boolean.TRUE.equals(request.getGachNoHoaDon());
        List<HoaDon> hoaDonNo = layHoaDonChuaThanhToan(hopDongId);
        BigDecimal tongNo = BigDecimal.ZERO;

        if (gachNo && !hoaDonNo.isEmpty()) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (HoaDon hd : hoaDonNo) {
                tongNo = tongNo.add(hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO);
                hd.setTrangThai("DA_THANH_TOAN");
                hd.setNgayThanhToan(now);
                hoaDonRepository.save(hd);
            }
            log.info("[{}] Đã khấu trừ và gạch nợ {} hóa đơn (tổng: {} đ) khi thanh lý hợp đồng {}",
                    threadName, hoaDonNo.size(), tongNo, hopDong.getMaHopDong());
        }

        // Thực hiện thanh lý hợp đồng & trả phòng về TRONG
        thanhLyHopDong(hopDongId);

        BigDecimal tienCoc = hopDong.getTienDatCoc() != null ? hopDong.getTienDatCoc() : BigDecimal.ZERO;
        BigDecimal thucHoan = tienCoc.subtract(tongNo).subtract(chiPhiKhauTru);

        log.info("[{}] Quyết toán hợp đồng {}: Cọc={}, Nợ HĐ={}, Khấu trừ={}, Thực hoàn={}",
                threadName, hopDong.getMaHopDong(), tienCoc, tongNo, chiPhiKhauTru, thucHoan);

        return new QuyetToanHopDongDTO(
                hopDong.getId(),
                hopDong.getMaHopDong(),
                hopDong.getPhongTro().getMaPhong(),
                hopDong.getKhachThue().getHoTen(),
                tienCoc,
                tongNo,
                hoaDonNo.size(),
                chiPhiKhauTru,
                request != null ? request.getLyDoKhauTru() : null,
                thucHoan,
                gachNo,
                LocalDate.now()
        );
    }
}
