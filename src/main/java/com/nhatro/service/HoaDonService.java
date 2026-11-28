package com.nhatro.service;

import com.nhatro.dto.ChiSoDienNuocDTO;
import com.nhatro.dto.HoaDonCreateDTO;
import com.nhatro.entity.HoaDon;
import com.nhatro.entity.HopDong;
import com.nhatro.exception.BusinessException;
import com.nhatro.repository.HoaDonRepository;
import com.nhatro.repository.HopDongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý toàn diện phân hệ Quản lý Chỉ số Điện Nước, Lập hóa đơn và Thu ngân.
 * Người phụ trách: Trương Đỗ Minh Quân (MSSV: 2001240390).
 */
@Service
public class HoaDonService {

    private static final Logger log = LoggerFactory.getLogger(HoaDonService.class);

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HopDongRepository hopDongRepository;

    /**
     * Tra cứu danh sách hóa đơn theo từ khóa, tháng/năm, trạng thái kèm phân trang.
     */
    public Page<HoaDon> searchHoaDon(String keyword, String thangNam, String trangThai, Pageable pageable) {
        return hoaDonRepository.search(keyword, thangNam, trangThai, pageable);
    }

    /**
     * Lấy chi tiết hóa đơn theo ID.
     */
    public HoaDon getById(Integer id) {
        return hoaDonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn có ID = " + id));
    }

    /**
     * Lấy danh sách toàn bộ hợp đồng đang có hiệu lực pháp lý (HIEU_LUC).
     */
    public List<HopDong> getDanhSachHopDongHieuLuc() {
        return hopDongRepository.findByTrangThaiWithDetails("HIEU_LUC");
    }

    /**
     * Gợi ý chỉ số điện - nước cũ và giá thuê cho hợp đồng được chọn.
     * Tự động lấy chỉ số điện/nước mới của tháng liền kề trước đó.
     */
    public ChiSoDienNuocDTO getChiSoDienNuoc(Integer hopDongId) {
        HopDong hopDong = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hợp đồng ID = " + hopDongId));

        Integer chiSoDienCu = 0;
        Integer chiSoNuocCu = 0;

        Optional<HoaDon> hoaDonTruocOpt = hoaDonRepository.findTopByHopDongIdOrderByNgayLapDesc(hopDongId);
        if (hoaDonTruocOpt.isPresent()) {
            HoaDon hdTruoc = hoaDonTruocOpt.get();
            chiSoDienCu = hdTruoc.getChiSoDienMoi() != null ? hdTruoc.getChiSoDienMoi() : 0;
            chiSoNuocCu = hdTruoc.getChiSoNuocMoi() != null ? hdTruoc.getChiSoNuocMoi() : 0;
        }

        String maPhong = (hopDong.getPhongTro() != null) ? hopDong.getPhongTro().getMaPhong() : "";
        String tenKhach = (hopDong.getKhachThue() != null) ? hopDong.getKhachThue().getHoTen() : "";

        return new ChiSoDienNuocDTO(
                hopDong.getId(),
                hopDong.getMaHopDong(),
                maPhong,
                tenKhach,
                hopDong.getGiaThueThoaThuan(),
                chiSoDienCu,
                chiSoNuocCu
        );
    }

    /**
     * Lập hóa đơn tiền phòng và chốt số điện nước định kỳ.
     * Kiểm soát nghiêm ngặt ràng buộc duy nhất UNIQUE(hop_dong_id, thang_nam) và logic chỉ số mới >= cũ.
     */
    @Transactional
    public HoaDon taoHoaDon(HoaDonCreateDTO dto) {
        if (dto.getHopDongId() == null) {
            throw new BusinessException("Vui lòng chọn hợp đồng thuê phòng!");
        }
        if (dto.getThangNam() == null || dto.getThangNam().trim().isEmpty()) {
            throw new BusinessException("Tháng/Năm lập hóa đơn không được để trống!");
        }

        String thangNamChuan = dto.getThangNam().trim();

        // 1. Kiểm tra sự tồn tại và trạng thái của hợp đồng
        HopDong hopDong = hopDongRepository.findById(dto.getHopDongId())
                .orElseThrow(() -> new BusinessException("Hợp đồng thuê không tồn tại trên hệ thống!"));

        if (!"HIEU_LUC".equals(hopDong.getTrangThai())) {
            throw new BusinessException("Hợp đồng " + hopDong.getMaHopDong() + " hiện không ở trạng thái HIEU_LUC!");
        }

        // 2. Kiểm soát ràng buộc UNIQUE(hop_dong_id, thang_nam)
        if (hoaDonRepository.existsByHopDongIdAndThangNam(dto.getHopDongId(), thangNamChuan)) {
            String tenPhong = (hopDong.getPhongTro() != null) ? hopDong.getPhongTro().getMaPhong() : "";
            throw new BusinessException("Hợp đồng " + hopDong.getMaHopDong() + " (Phòng " + tenPhong + ") đã có hóa đơn trong tháng " + thangNamChuan + "! Không thể lập trùng.");
        }

        // 3. Kiểm tra chỉ số điện - nước hợp lệ (mới >= cũ, không âm) và tính lượng tiêu thụ
        int chiSoDienCu = (dto.getChiSoDienCu() != null) ? dto.getChiSoDienCu() : 0;
        int chiSoDienMoi = (dto.getChiSoDienMoi() != null) ? dto.getChiSoDienMoi() : 0;
        if (chiSoDienCu < 0 || chiSoDienMoi < 0) {
            throw new BusinessException("Chỉ số điện không được là số âm!");
        }
        if (chiSoDienMoi < chiSoDienCu) {
            throw new BusinessException("Chỉ số điện mới (" + chiSoDienMoi + ") không được nhỏ hơn chỉ số điện cũ (" + chiSoDienCu + ")!");
        }
        int soDien = chiSoDienMoi - chiSoDienCu;

        int chiSoNuocCu = (dto.getChiSoNuocCu() != null) ? dto.getChiSoNuocCu() : 0;
        int chiSoNuocMoi = (dto.getChiSoNuocMoi() != null) ? dto.getChiSoNuocMoi() : 0;
        if (chiSoNuocCu < 0 || chiSoNuocMoi < 0) {
            throw new BusinessException("Chỉ số nước không được là số âm!");
        }
        if (chiSoNuocMoi < chiSoNuocCu) {
            throw new BusinessException("Chỉ số nước mới (" + chiSoNuocMoi + ") không được nhỏ hơn chỉ số nước cũ (" + chiSoNuocCu + ")!");
        }
        int soNuoc = chiSoNuocMoi - chiSoNuocCu;

        BigDecimal donGiaDien = (dto.getDonGiaDien() != null && dto.getDonGiaDien().compareTo(BigDecimal.ZERO) >= 0)
                ? dto.getDonGiaDien() : new BigDecimal("3500");
        BigDecimal donGiaNuoc = (dto.getDonGiaNuoc() != null && dto.getDonGiaNuoc().compareTo(BigDecimal.ZERO) >= 0)
                ? dto.getDonGiaNuoc() : new BigDecimal("20000");
        BigDecimal tienDichVu = (dto.getTienDichVu() != null && dto.getTienDichVu().compareTo(BigDecimal.ZERO) >= 0)
                ? dto.getTienDichVu() : new BigDecimal("100000");
        BigDecimal tienPhong = (dto.getTienPhong() != null && dto.getTienPhong().compareTo(BigDecimal.ZERO) > 0)
                ? dto.getTienPhong() : hopDong.getGiaThueThoaThuan();

        // 4. Tính toán số tiền chi tiết
        BigDecimal tienDien = BigDecimal.valueOf(soDien).multiply(donGiaDien);
        BigDecimal tienNuoc = BigDecimal.valueOf(soNuoc).multiply(donGiaNuoc);

        BigDecimal tongTien = tienPhong.add(tienDien).add(tienNuoc).add(tienDichVu);

        // 5. Sinh mã hóa đơn chuẩn duy nhất
        String cleanThang = thangNamChuan.replace("/", "");
        String maPhong = (hopDong.getPhongTro() != null) ? hopDong.getPhongTro().getMaPhong() : "P";
        String maHoaDon = "HD-" + cleanThang + "-" + maPhong;
        if (hoaDonRepository.existsByMaHoaDon(maHoaDon)) {
            maHoaDon += "-" + (System.currentTimeMillis() % 10000);
        }

        // 6. Khởi tạo đối tượng HoaDon và lưu cơ sở dữ liệu
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setHopDong(hopDong);
        hoaDon.setThangNam(thangNamChuan);
        hoaDon.setChiSoDienCu(chiSoDienCu);
        hoaDon.setChiSoDienMoi(chiSoDienMoi);
        hoaDon.setDonGiaDien(donGiaDien);
        hoaDon.setChiSoNuocCu(chiSoNuocCu);
        hoaDon.setChiSoNuocMoi(chiSoNuocMoi);
        hoaDon.setDonGiaNuoc(donGiaNuoc);
        hoaDon.setTienPhong(tienPhong);
        hoaDon.setTienDien(tienDien);
        hoaDon.setTienNuoc(tienNuoc);
        hoaDon.setTienDichVu(tienDichVu);
        hoaDon.setTongTien(tongTien);
        hoaDon.setTrangThai("CHUA_THANH_TOAN");
        hoaDon.setNgayLap(LocalDateTime.now());
        hoaDon.setNgayThanhToan(null);

        HoaDon saved = hoaDonRepository.save(hoaDon);
        log.info("Lập thành công hóa đơn {} cho hợp đồng {} (Phòng {}), tổng tiền: {} đ",
                saved.getMaHoaDon(), hopDong.getMaHopDong(), maPhong, saved.getTongTien());
        return saved;
    }

    /**
     * Nghiệp vụ Thu ngân: Xác nhận thanh toán (Gạch nợ) hóa đơn.
     */
    @Transactional
    public void xacNhanThanhToan(Integer hoaDonId) {
        HoaDon hoaDon = getById(hoaDonId);
        if ("DA_THANH_TOAN".equals(hoaDon.getTrangThai())) {
            throw new BusinessException("Hóa đơn " + hoaDon.getMaHoaDon() + " đã được thanh toán trước đó!");
        }

        hoaDon.setTrangThai("DA_THANH_TOAN");
        hoaDon.setNgayThanhToan(LocalDateTime.now());
        hoaDonRepository.save(hoaDon);
        log.info("Gạch nợ thành công hóa đơn: {} vào lúc: {}", hoaDon.getMaHoaDon(), hoaDon.getNgayThanhToan());
    }

    /**
     * Hủy xác nhận thanh toán (hoàn tác gạch nợ) nếu thu ngân thao tác nhầm.
     */
    @Transactional
    public void huyThanhToan(Integer hoaDonId) {
        HoaDon hoaDon = getById(hoaDonId);
        hoaDon.setTrangThai("CHUA_THANH_TOAN");
        hoaDon.setNgayThanhToan(null);
        hoaDonRepository.save(hoaDon);
        log.info("Hủy thanh toán hóa đơn: {}", hoaDon.getMaHoaDon());
    }

    public long countByTrangThai(String trangThai) {
        return hoaDonRepository.countByTrangThai(trangThai);
    }

    public BigDecimal sumTongDoanhThuDaThu() {
        return hoaDonRepository.sumTongDoanhThuDaThu();
    }
}
