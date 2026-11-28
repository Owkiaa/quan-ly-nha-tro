package com.nhatro.service;

import com.nhatro.dto.ChiSoDienNuocDTO;
import com.nhatro.dto.HoaDonBatchFormDTO;
import com.nhatro.dto.HoaDonBatchItemDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        HopDong hopDong = hopDongRepository.findById(dto.getHopDongId())
                .orElseThrow(() -> new BusinessException("Hợp đồng thuê không tồn tại trên hệ thống!"));

        if (hoaDonRepository.existsByHopDongIdAndThangNam(dto.getHopDongId(), thangNamChuan)) {
            String tenPhong = (hopDong.getPhongTro() != null) ? hopDong.getPhongTro().getMaPhong() : "";
            throw new BusinessException("Hợp đồng " + hopDong.getMaHopDong() + " (Phòng " + tenPhong + ") đã có hóa đơn trong tháng " + thangNamChuan + "! Không thể lập trùng.");
        }

        HoaDon hoaDon = createHoaDonEntity(
                hopDong,
                thangNamChuan,
                dto.getChiSoDienCu(),
                dto.getChiSoDienMoi(),
                dto.getChiSoNuocCu(),
                dto.getChiSoNuocMoi(),
                dto.getDonGiaDien(),
                dto.getDonGiaNuoc(),
                dto.getTienPhong(),
                dto.getTienDichVu(),
                null
        );

        HoaDon saved = hoaDonRepository.save(hoaDon);
        String maPhong = (hopDong.getPhongTro() != null) ? hopDong.getPhongTro().getMaPhong() : "P";
        log.info("Lập thành công hóa đơn {} cho hợp đồng {} (Phòng {}), tổng tiền: {} đ",
                saved.getMaHoaDon(), hopDong.getMaHopDong(), maPhong, saved.getTongTien());
        return saved;
    }

    public HoaDonBatchFormDTO prepareBatchForm(String thangNam) {
        String thangNamChuan = (thangNam != null && !thangNam.trim().isEmpty())
                ? thangNam.trim()
                : LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy"));

        List<HopDong> activeContracts = hopDongRepository.findByTrangThaiWithDetails("HIEU_LUC");
        Map<Integer, HoaDon> existingInMonthMap = buildExistingInMonthMap(thangNamChuan);
        Map<Integer, HoaDon> latestByHopDong = buildLatestByHopDongMap(activeContracts);

        HoaDonBatchFormDTO form = new HoaDonBatchFormDTO();
        form.setThangNam(thangNamChuan);
        form.setDonGiaDien(new BigDecimal("3500"));
        form.setDonGiaNuoc(new BigDecimal("20000"));

        List<HoaDonBatchItemDTO> items = new ArrayList<>();
        for (HopDong hd : activeContracts) {
            HoaDonBatchItemDTO item = new HoaDonBatchItemDTO();
            item.setHopDongId(hd.getId());
            item.setMaHopDong(hd.getMaHopDong());
            item.setMaPhong(hd.getPhongTro() != null ? hd.getPhongTro().getMaPhong() : "");
            item.setTenKhachThue(hd.getKhachThue() != null ? hd.getKhachThue().getHoTen() : "");

            HoaDon existing = existingInMonthMap.get(hd.getId());
            if (existing != null) {
                item.setSelected(false);
                item.setDaCoHoaDon(true);
                item.setExistingHoaDonId(existing.getId());
                item.setExistingMaHoaDon(existing.getMaHoaDon());
                item.setChiSoDienCu(existing.getChiSoDienCu());
                item.setChiSoDienMoi(existing.getChiSoDienMoi());
                item.setChiSoNuocCu(existing.getChiSoNuocCu());
                item.setChiSoNuocMoi(existing.getChiSoNuocMoi());
                item.setTienPhong(existing.getTienPhong());
                item.setTienDichVu(existing.getTienDichVu());
            } else {
                HoaDon latest = latestByHopDong.get(hd.getId());
                int dienCu = (latest != null && latest.getChiSoDienMoi() != null) ? latest.getChiSoDienMoi() : 0;
                int nuocCu = (latest != null && latest.getChiSoNuocMoi() != null) ? latest.getChiSoNuocMoi() : 0;
                item.setSelected(true);
                item.setDaCoHoaDon(false);
                item.setChiSoDienCu(dienCu);
                item.setChiSoDienMoi(dienCu);
                item.setChiSoNuocCu(nuocCu);
                item.setChiSoNuocMoi(nuocCu);
                item.setTienPhong(hd.getGiaThueThoaThuan() != null ? hd.getGiaThueThoaThuan() : BigDecimal.ZERO);
                item.setTienDichVu(new BigDecimal("100000"));
            }
            items.add(item);
        }

        form.setItems(items);
        return form;
    }

    public void refreshBatchItemMetadata(HoaDonBatchFormDTO form) {
        if (form == null || form.getItems() == null || form.getItems().isEmpty()) {
            return;
        }
        String thangNamChuan = (form.getThangNam() != null) ? form.getThangNam().trim() : "";
        Map<Integer, HoaDon> existingInMonthMap = thangNamChuan.isEmpty()
                ? new HashMap<>()
                : buildExistingInMonthMap(thangNamChuan);

        Map<Integer, HopDong> hopDongMap = new HashMap<>();
        for (HopDong hd : hopDongRepository.findByTrangThaiWithDetails("HIEU_LUC")) {
            hopDongMap.put(hd.getId(), hd);
        }

        for (HoaDonBatchItemDTO item : form.getItems()) {
            if (item.getHopDongId() == null) {
                continue;
            }
            HopDong hd = hopDongMap.get(item.getHopDongId());
            if (hd != null) {
                item.setMaHopDong(hd.getMaHopDong());
                item.setMaPhong(hd.getPhongTro() != null ? hd.getPhongTro().getMaPhong() : "");
                item.setTenKhachThue(hd.getKhachThue() != null ? hd.getKhachThue().getHoTen() : "");
            }
            HoaDon existing = existingInMonthMap.get(item.getHopDongId());
            if (existing != null) {
                item.setDaCoHoaDon(true);
                item.setExistingHoaDonId(existing.getId());
                item.setExistingMaHoaDon(existing.getMaHoaDon());
                item.setSelected(false);
            } else {
                item.setDaCoHoaDon(false);
                item.setExistingHoaDonId(null);
                item.setExistingMaHoaDon(null);
            }
        }
    }

    @Transactional
    public List<HoaDon> taoHoaDonHangLoat(HoaDonBatchFormDTO form) {
        if (form == null || form.getThangNam() == null || form.getThangNam().trim().isEmpty()) {
            throw new BusinessException("Tháng/Năm lập hóa đơn không được để trống!");
        }

        String thangNamChuan = form.getThangNam().trim();
        List<HoaDonBatchItemDTO> selectedItems = new ArrayList<>();
        List<Integer> selectedHopDongIds = new ArrayList<>();
        if (form.getItems() != null) {
            for (HoaDonBatchItemDTO item : form.getItems()) {
                if (item.isSelected() && item.getHopDongId() != null) {
                    selectedItems.add(item);
                    selectedHopDongIds.add(item.getHopDongId());
                }
            }
        }

        if (selectedItems.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất một phòng để lập hóa đơn!");
        }

        Map<Integer, HopDong> hopDongMap = new HashMap<>();
        for (HopDong hd : hopDongRepository.findAllById(selectedHopDongIds)) {
            hopDongMap.put(hd.getId(), hd);
        }

        Map<Integer, HoaDon> existingInMonthMap = buildExistingInMonthMap(thangNamChuan);
        Set<String> usedCodesInBatch = new HashSet<>();
        List<HoaDon> toSave = new ArrayList<>();

        for (HoaDonBatchItemDTO item : selectedItems) {
            HopDong hopDong = hopDongMap.get(item.getHopDongId());
            if (hopDong == null) {
                throw new BusinessException("Hợp đồng ID = " + item.getHopDongId() + " không tồn tại trên hệ thống!");
            }
            if (existingInMonthMap.containsKey(hopDong.getId())) {
                String tenPhong = (hopDong.getPhongTro() != null) ? hopDong.getPhongTro().getMaPhong() : "";
                throw new BusinessException("Hợp đồng " + hopDong.getMaHopDong() + " (Phòng " + tenPhong + ") đã có hóa đơn trong tháng " + thangNamChuan + "! Không thể lập trùng.");
            }

            HoaDon hoaDon = createHoaDonEntity(
                    hopDong,
                    thangNamChuan,
                    item.getChiSoDienCu(),
                    item.getChiSoDienMoi(),
                    item.getChiSoNuocCu(),
                    item.getChiSoNuocMoi(),
                    form.getDonGiaDien(),
                    form.getDonGiaNuoc(),
                    item.getTienPhong(),
                    item.getTienDichVu(),
                    usedCodesInBatch
            );
            toSave.add(hoaDon);
        }

        List<HoaDon> savedList = hoaDonRepository.saveAll(toSave);
        log.info("Lập hàng loạt thành công {} hóa đơn cho kỳ {}", savedList.size(), thangNamChuan);
        return savedList;
    }

    private Map<Integer, HoaDon> buildExistingInMonthMap(String thangNam) {
        Map<Integer, HoaDon> map = new HashMap<>();
        for (HoaDon hd : hoaDonRepository.findByThangNamWithHopDong(thangNam)) {
            if (hd.getHopDong() != null) {
                map.putIfAbsent(hd.getHopDong().getId(), hd);
            }
        }
        return map;
    }

    private Map<Integer, HoaDon> buildLatestByHopDongMap(List<HopDong> contracts) {
        Map<Integer, HoaDon> latestMap = new HashMap<>();
        if (contracts.isEmpty()) {
            return latestMap;
        }
        List<Integer> ids = new ArrayList<>();
        for (HopDong hd : contracts) {
            ids.add(hd.getId());
        }
        for (HoaDon hd : hoaDonRepository.findLatestByHopDongIds(ids)) {
            if (hd.getHopDong() != null) {
                latestMap.putIfAbsent(hd.getHopDong().getId(), hd);
            }
        }
        return latestMap;
    }

    private HoaDon createHoaDonEntity(
            HopDong hopDong,
            String thangNamChuan,
            Integer chiSoDienCuInput,
            Integer chiSoDienMoiInput,
            Integer chiSoNuocCuInput,
            Integer chiSoNuocMoiInput,
            BigDecimal donGiaDienInput,
            BigDecimal donGiaNuocInput,
            BigDecimal tienPhongInput,
            BigDecimal tienDichVuInput,
            Set<String> usedCodesInBatch
    ) {
        if (!"HIEU_LUC".equals(hopDong.getTrangThai())) {
            throw new BusinessException("Hợp đồng " + hopDong.getMaHopDong() + " hiện không ở trạng thái HIEU_LUC!");
        }

        String maPhong = (hopDong.getPhongTro() != null) ? hopDong.getPhongTro().getMaPhong() : "P";

        int chiSoDienCu = (chiSoDienCuInput != null) ? chiSoDienCuInput : 0;
        int chiSoDienMoi = (chiSoDienMoiInput != null) ? chiSoDienMoiInput : 0;
        if (chiSoDienCu < 0 || chiSoDienMoi < 0) {
            throw new BusinessException("Phòng " + maPhong + ": Chỉ số điện không được là số âm!");
        }
        if (chiSoDienMoi < chiSoDienCu) {
            throw new BusinessException("Phòng " + maPhong + ": Chỉ số điện mới (" + chiSoDienMoi + ") không được nhỏ hơn chỉ số điện cũ (" + chiSoDienCu + ")!");
        }
        int soDien = chiSoDienMoi - chiSoDienCu;

        int chiSoNuocCu = (chiSoNuocCuInput != null) ? chiSoNuocCuInput : 0;
        int chiSoNuocMoi = (chiSoNuocMoiInput != null) ? chiSoNuocMoiInput : 0;
        if (chiSoNuocCu < 0 || chiSoNuocMoi < 0) {
            throw new BusinessException("Phòng " + maPhong + ": Chỉ số nước không được là số âm!");
        }
        if (chiSoNuocMoi < chiSoNuocCu) {
            throw new BusinessException("Phòng " + maPhong + ": Chỉ số nước mới (" + chiSoNuocMoi + ") không được nhỏ hơn chỉ số nước cũ (" + chiSoNuocCu + ")!");
        }
        int soNuoc = chiSoNuocMoi - chiSoNuocCu;

        BigDecimal donGiaDien = (donGiaDienInput != null && donGiaDienInput.compareTo(BigDecimal.ZERO) >= 0)
                ? donGiaDienInput : new BigDecimal("3500");
        BigDecimal donGiaNuoc = (donGiaNuocInput != null && donGiaNuocInput.compareTo(BigDecimal.ZERO) >= 0)
                ? donGiaNuocInput : new BigDecimal("20000");
        BigDecimal tienDichVu = (tienDichVuInput != null && tienDichVuInput.compareTo(BigDecimal.ZERO) >= 0)
                ? tienDichVuInput : new BigDecimal("100000");
        BigDecimal tienPhong = (tienPhongInput != null && tienPhongInput.compareTo(BigDecimal.ZERO) > 0)
                ? tienPhongInput : hopDong.getGiaThueThoaThuan();

        BigDecimal tienDien = BigDecimal.valueOf(soDien).multiply(donGiaDien);
        BigDecimal tienNuoc = BigDecimal.valueOf(soNuoc).multiply(donGiaNuoc);
        BigDecimal tongTien = tienPhong.add(tienDien).add(tienNuoc).add(tienDichVu);

        String cleanThang = thangNamChuan.replace("/", "");
        String baseMaHoaDon = "HD-" + cleanThang + "-" + maPhong;
        String maHoaDon = baseMaHoaDon;
        if (hoaDonRepository.existsByMaHoaDon(maHoaDon) || (usedCodesInBatch != null && usedCodesInBatch.contains(maHoaDon))) {
            long suffix = System.currentTimeMillis() % 10000;
            maHoaDon = baseMaHoaDon + "-" + suffix;
            int attempt = 1;
            while (hoaDonRepository.existsByMaHoaDon(maHoaDon) || (usedCodesInBatch != null && usedCodesInBatch.contains(maHoaDon))) {
                maHoaDon = baseMaHoaDon + "-" + suffix + "-" + attempt++;
            }
        }
        if (usedCodesInBatch != null) {
            usedCodesInBatch.add(maHoaDon);
        }

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

        return hoaDon;
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

    @Transactional
    public void xoaHoaDon(Integer hoaDonId) {
        HoaDon hoaDon = getById(hoaDonId);
        if ("DA_THANH_TOAN".equals(hoaDon.getTrangThai())) {
            throw new BusinessException("Không thể xóa hóa đơn " + hoaDon.getMaHoaDon() + " do đã được thanh toán! Vui lòng hoàn tác thanh toán trước nếu cần hủy.");
        }
        if (!"CHUA_THANH_TOAN".equals(hoaDon.getTrangThai())) {
            throw new BusinessException("Chỉ được phép xóa hóa đơn ở trạng thái CHƯA THANH TOÁN!");
        }
        hoaDonRepository.delete(hoaDon);
        log.info("Xóa thành công hóa đơn nháp: {}", hoaDon.getMaHoaDon());
    }

    public long countByTrangThai(String trangThai) {
        return hoaDonRepository.countByTrangThai(trangThai);
    }

    public BigDecimal sumTongDoanhThuDaThu() {
        return hoaDonRepository.sumTongDoanhThuDaThu();
    }

    public BigDecimal sumTongNoChuaThu() {
        return hoaDonRepository.sumTongNoChuaThu();
    }
}
