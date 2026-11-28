package com.nhatro.service;

import com.nhatro.dto.DatChoRequestDTO;
import com.nhatro.entity.DatCho;
import com.nhatro.entity.KhachThue;
import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.RoomAlreadyBookedException;
import com.nhatro.repository.DatChoRepository;
import com.nhatro.repository.KhachThueRepository;
import com.nhatro.repository.PhongTroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý nghiệp vụ Đặt giữ chỗ phòng trọ và cơ chế khóa đồng thời (Pessimistic Lock).
 * Phụ trách bởi: Bùi Trần Tấn Tài
 */
@Service
public class DatChoService {

    private static final Logger log = LoggerFactory.getLogger(DatChoService.class);

    public static final String TRANG_THAI_DANG_GIU = "DANG_GIU";
    public static final String TRANG_THAI_HET_HAN = "HET_HAN";
    public static final String TRANG_THAI_DA_CHUYEN_HOP_DONG = "DA_CHUYEN_HOP_DONG";

    public static final String PHONG_TRONG = "TRONG";
    public static final String PHONG_DA_DAT = "DA_DAT";
    public static final String PHONG_DA_THUE = "DA_THUE";

    @Autowired
    private PhongTroRepository phongTroRepository;

    @Autowired
    private DatChoRepository datChoRepository;

    @Autowired
    private KhachThueRepository khachThueRepository;

    @Autowired(required = false)
    private DatChoMonitor datChoMonitor;

    private final Object bookingLock = new Object();

    /**
     * Quy trình đặt giữ chỗ phòng kết hợp Monitor Lock (synchronized Chương 4) và Pessimistic Write Lock (findByIdForUpdate).
     * Đảm bảo an toàn khi nhiều Client Web hoặc Socket gửi lệnh BOOK đồng thời.
     */
    @Transactional
    public DatCho datChoPhong(Integer phongId, String hoTen, String cccd, String soDienThoai,
                             String email, String ghiChu, Integer soGioGiuCho) {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Tiếp nhận yêu cầu đặt giữ chỗ cho phòng ID: {}", threadName, phongId);

        if (phongId == null) {
            throw new BusinessException("Mã ID phòng trọ không được để trống.");
        }
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new BusinessException("Họ tên khách thuê không được để trống.");
        }
        if (cccd == null || cccd.trim().isEmpty()) {
            throw new BusinessException("Số CCCD khách thuê không được để trống.");
        }
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            throw new BusinessException("Số điện thoại khách thuê không được để trống.");
        }

        // 1. Đồng bộ vùng găng bằng Monitor Lock Java (synchronized) và khóa dòng dữ liệu SQL Server bằng Pessimistic Write Lock
        synchronized (bookingLock) {
            PhongTro phong = phongTroRepository.findByIdForUpdate(phongId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy phòng trọ với ID: " + phongId));

            // 2. Kiểm tra trạng thái phòng: chỉ cho phép đặt phòng đang TRONG
            if (!PHONG_TRONG.equalsIgnoreCase(phong.getTrangThai())) {
                log.warn("[{}] Phòng {} (ID: {}) không ở trạng thái TRONG (hiện tại: {})",
                        threadName, phong.getMaPhong(), phongId, phong.getTrangThai());
                if (datChoMonitor != null) {
                    datChoMonitor.ghiNhanDatPhongBiTuChoi(
                            phong.getMaPhong(), "Phòng đang ở trạng thái " + phong.getTrangThai(), ghiChu);
                }
                throw new RoomAlreadyBookedException("Phòng " + phong.getMaPhong() +
                        " đã được đặt hoặc đang thuê (Trạng thái: " + phong.getTrangThai() + ").");
            }

            // 3. Tìm hoặc tạo mới hồ sơ khách thuê theo CCCD
            String cleanCccd = cccd.trim();
            KhachThue khachThue = khachThueRepository.findByCccd(cleanCccd)
                    .orElseGet(() -> {
                        log.info("[{}] Khách thuê với CCCD {} chưa tồn tại trong CSDL, tiến hành tạo mới...", threadName, cleanCccd);
                        KhachThue moi = new KhachThue();
                        moi.setHoTen(hoTen.trim());
                        moi.setCccd(cleanCccd);
                        moi.setSoDienThoai(soDienThoai.trim());
                        moi.setEmail(email != null ? email.trim() : null);
                        moi.setTrangThai("DANG_THUE");
                        return khachThueRepository.save(moi);
                    });

            // Cập nhật thông tin liên lạc nếu khách đã tồn tại
            boolean changed = false;
            if (hoTen != null && !hoTen.trim().isEmpty() && !hoTen.trim().equalsIgnoreCase(khachThue.getHoTen())) {
                khachThue.setHoTen(hoTen.trim());
                changed = true;
            }
            if (soDienThoai != null && !soDienThoai.trim().isEmpty() && !soDienThoai.trim().equalsIgnoreCase(khachThue.getSoDienThoai())) {
                khachThue.setSoDienThoai(soDienThoai.trim());
                changed = true;
            }
            if (email != null && !email.trim().isEmpty() && !email.trim().equalsIgnoreCase(khachThue.getEmail())) {
                khachThue.setEmail(email.trim());
                changed = true;
            }
            if (changed) {
                khachThueRepository.save(khachThue);
            }

            // 4. Tạo bản ghi đặt chỗ trong bảng dat_cho
            int holdingHours = (soGioGiuCho != null && soGioGiuCho > 0) ? soGioGiuCho : 24;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime hanGiuCho = now.plusHours(holdingHours);

            DatCho datCho = new DatCho();
            datCho.setPhongTro(phong);
            datCho.setKhachThue(khachThue);
            datCho.setNgayDat(now);
            datCho.setHanGiuCho(hanGiuCho);
            datCho.setTrangThai(TRANG_THAI_DANG_GIU);
            datCho.setGhiChu(ghiChu);
            DatCho savedDatCho = datChoRepository.save(datCho);

            // 5. Cập nhật trạng thái phòng sang DA_DAT
            phong.setTrangThai(PHONG_DA_DAT);
            phongTroRepository.save(phong);

            if (datChoMonitor != null) {
                datChoMonitor.ghiNhanDatPhongThanhCong(phong.getMaPhong(), khachThue.getHoTen(), cleanCccd, ghiChu);
            }

            log.info("[{}] Đặt giữ chỗ THÀNH CÔNG cho phòng {} (ID: {}) bởi khách {} (CCCD: {}), hạn giữ chỗ đến: {}",
                    threadName, phong.getMaPhong(), phongId, khachThue.getHoTen(), cleanCccd, hanGiuCho);

            return savedDatCho;
        }
    }

    /**
     * Đặt phòng theo mã phòng (phục vụ trực tiếp cho lệnh BOOK của Socket Server).
     */
    @Transactional
    public DatCho datChoTheoMaPhong(String maPhong, String hoTen, String cccd, String soDienThoai) {
        if (maPhong == null || maPhong.trim().isEmpty()) {
            throw new BusinessException("Mã phòng không được để trống.");
        }
        Integer phongId = phongTroRepository.findIdByMaPhong(maPhong.trim())
                .orElseThrow(() -> new BusinessException("Không tìm thấy phòng trọ với mã: " + maPhong));
        return datChoPhong(phongId, hoTen, cccd, soDienThoai, null, "Đặt qua Socket Client", 24);
    }

    /**
     * Đặt phòng thông qua DatChoRequestDTO (phục vụ Web Controller).
     */
    @Transactional
    public DatCho datCho(DatChoRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Dữ liệu yêu cầu đặt chỗ không hợp lệ.");
        }
        Integer phongId = request.getPhongId();
        if (phongId == null && request.getMaPhong() != null && !request.getMaPhong().trim().isEmpty()) {
            phongId = phongTroRepository.findIdByMaPhong(request.getMaPhong().trim())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy phòng trọ với mã: " + request.getMaPhong()));
        }
        return datChoPhong(phongId, request.getHoTen(), request.getCccd(),
                request.getSoDienThoai(), request.getEmail(), request.getGhiChu(), request.getSoGioGiuCho());
    }

    /**
     * Hủy đặt giữ chỗ thủ công và đưa phòng trở lại trạng thái TRONG.
     */
    @Transactional
    public void huyDatCho(Integer datChoId, String lyDoHuy) {
        String threadName = Thread.currentThread().getName();
        DatCho datCho = datChoRepository.findById(datChoId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bản ghi đặt chỗ ID: " + datChoId));

        if (!TRANG_THAI_DANG_GIU.equals(datCho.getTrangThai())) {
            throw new BusinessException("Bản ghi đặt chỗ không ở trạng thái ĐANG GIỮ (hiện tại: " + datCho.getTrangThai() + ").");
        }

        PhongTro phong = phongTroRepository.findByIdForUpdate(datCho.getPhongTro().getId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy phòng trọ liên kết với đặt chỗ này."));

        datCho.setTrangThai(TRANG_THAI_HET_HAN);
        if (lyDoHuy != null && !lyDoHuy.trim().isEmpty()) {
            String oldGhiChu = datCho.getGhiChu() != null ? datCho.getGhiChu() + " | " : "";
            datCho.setGhiChu(oldGhiChu + "Hủy thủ công: " + lyDoHuy.trim());
        }
        datChoRepository.save(datCho);

        phong.setTrangThai(PHONG_TRONG);
        phongTroRepository.save(phong);
        log.info("[{}] Đã hủy đặt chỗ ID: {}, phòng {} trở về trạng thái TRONG", threadName, datChoId, phong.getMaPhong());
    }

    /**
     * Giải phóng một bản ghi đặt giữ chỗ quá hạn trong một Transaction độc lập.
     * Phương thức này được gọi qua Bean Proxy của DatChoService từ DatChoScheduler
     * để đảm bảo @Transactional và findByIdForUpdate (Pessimistic Lock) hoạt động chính xác.
     */
    @Transactional
    public void releaseSingleBooking(Integer datChoId, Integer phongId) {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Đang kiểm tra và giải phóng phòng ID: {} (Đặt chỗ ID: {})", threadName, phongId, datChoId);

        // 1. Khóa phòng trọ cần giải phóng bằng Pessimistic Lock
        PhongTro phong = phongTroRepository.findByIdForUpdate(phongId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phòng trọ ID: " + phongId));

        // 2. Kiểm tra lại trong transaction: đảm bảo bản ghi dat_cho vẫn còn ở trạng thái DANG_GIU
        DatCho datCho = datChoRepository.findById(datChoId).orElse(null);
        if (datCho != null && TRANG_THAI_DANG_GIU.equals(datCho.getTrangThai())) {
            datCho.setTrangThai(TRANG_THAI_HET_HAN);
            String oldGhiChu = datCho.getGhiChu() != null ? datCho.getGhiChu() + " | " : "";
            datCho.setGhiChu(oldGhiChu + "Tự động hủy do hết hạn giữ chỗ");
            datChoRepository.save(datCho);

            // 3. Đưa phòng trở về trạng thái TRONG nếu phòng đang ở DA_DAT
            if (PHONG_DA_DAT.equals(phong.getTrangThai())) {
                phong.setTrangThai(PHONG_TRONG);
                phongTroRepository.save(phong);
                if (datChoMonitor != null) {
                    datChoMonitor.ghiNhanGiaiPhongQuaHan(phong.getMaPhong(), datChoId);
                }
                log.info("[{}] ĐÃ GIẢI PHÓNG PHÒNG {} (ID: {}) VỀ TRẠNG THÁI TRONG THÀNH CÔNG",
                        threadName, phong.getMaPhong(), phongId);
            } else {
                log.info("[{}] Đặt chỗ ID: {} đã hết hạn, phòng {} hiện ở trạng thái: {}",
                        threadName, datChoId, phong.getMaPhong(), phong.getTrangThai());
            }
        } else {
            log.info("[{}] Bỏ qua giải phóng đặt chỗ ID {}: không tồn tại hoặc không còn là DANG_GIU",
                    threadName, datChoId);
        }
    }

    /**
     * Tìm kiếm và phân trang danh sách đặt chỗ.
     */
    public Page<DatCho> timKiem(String keyword, String trangThai, Pageable pageable) {
        return datChoRepository.search(keyword, trangThai, pageable);
    }

    /**
     * Tìm đặt chỗ theo ID.
     */
    public Optional<DatCho> timTheoId(Integer id) {
        return datChoRepository.findById(id);
    }

    /**
     * Lấy toàn bộ danh sách đặt chỗ.
     */
    public List<DatCho> layTatCa() {
        return datChoRepository.findAll();
    }

    /**
     * Lấy danh sách phòng trống sẵn sàng cho đặt giữ chỗ.
     */
    public List<PhongTro> layDanhSachPhongTrong() {
        return phongTroRepository.findByTrangThaiOrderByMaPhongAsc(PHONG_TRONG);
    }

    public long demDangGiu() {
        return datChoRepository.countByTrangThai(TRANG_THAI_DANG_GIU);
    }

    public long demHetHan() {
        return datChoRepository.countByTrangThai(TRANG_THAI_HET_HAN);
    }

    public long demDaChuyenHopDong() {
        return datChoRepository.countByTrangThai(TRANG_THAI_DA_CHUYEN_HOP_DONG);
    }
}
