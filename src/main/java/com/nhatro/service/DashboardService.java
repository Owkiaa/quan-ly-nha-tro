package com.nhatro.service;

import com.nhatro.dto.DashboardDTO;
import com.nhatro.repository.HoaDonRepository;
import com.nhatro.repository.PhongTroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service tổng hợp số liệu phân tích tài chíh.
 * Người phụ trách: Trương Đỗ Minh Quân (MSSV: 2001240390).
 */
@Service
public class DashboardService {

    @Autowired
    private PhongTroRepository phongTroRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    public DashboardDTO getDashboardStats() {
        DashboardDTO dto = new DashboardDTO();

        long countTrong = phongTroRepository.countByTrangThai("TRONG");
        long countDaDat = phongTroRepository.countByTrangThai("DA_DAT");
        long countDaThue = phongTroRepository.countByTrangThai("DA_THUE");
        long countBaoTri = phongTroRepository.countByTrangThai("BAO_TRI");
        long tongPhong = countTrong + countDaDat + countDaThue + countBaoTri;

        dto.setTongSoPhong(tongPhong);
        dto.setSoPhongTrong(countTrong);
        dto.setSoPhongDaDat(countDaDat);
        dto.setSoPhongDangThue(countDaThue);
        dto.setSoPhongBaoTri(countBaoTri);

        double tiLeLapDay = (tongPhong > 0) ? ((double) countDaThue / tongPhong * 100.0) : 0.0;
        dto.setTiLeLapDay(Math.round(tiLeLapDay * 10.0) / 10.0);

        BigDecimal tongDaThu = hoaDonRepository.sumTongDoanhThuDaThu();
        BigDecimal tongNo = hoaDonRepository.sumTongNoChuaThu();
        dto.setTongDoanhThuDaThu(tongDaThu != null ? tongDaThu : BigDecimal.ZERO);
        dto.setTongNoChuaThu(tongNo != null ? tongNo : BigDecimal.ZERO);

        long hdDaThu = hoaDonRepository.countByTrangThai("DA_THANH_TOAN");
        long hdChuaThu = hoaDonRepository.countByTrangThai("CHUA_THANH_TOAN");
        dto.setSoHoaDonDaThanhToan(hdDaThu);
        dto.setSoHoaDonChuaThanhToan(hdChuaThu);

        String thangHienTai = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy"));
        BigDecimal doanhThuThang = hoaDonRepository.sumDoanhThuTheoThang(thangHienTai);
        dto.setDoanhThuThangNay(doanhThuThang != null ? doanhThuThang : BigDecimal.ZERO);
        List<String> thangLabels = new ArrayList<>();
        List<BigDecimal> doanhThuList = new ArrayList<>();
        List<BigDecimal> noList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate m = today.minusMonths(i);
            String label = m.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            thangLabels.add(label);

            BigDecimal dt = hoaDonRepository.sumDoanhThuTheoThang(label);
            doanhThuList.add(dt != null ? dt : BigDecimal.ZERO);

            BigDecimal no = hoaDonRepository.sumNoTheoThang(label);
            noList.add(no != null ? no : BigDecimal.ZERO);
        }

        dto.setBieuDoThangLabels(thangLabels);
        dto.setBieuDoDoanhThuValues(doanhThuList);
        dto.setBieuDoNoValues(noList);
        dto.setBieuDoPhongLabels(Arrays.asList("Đang cho thuê", "Phòng trống", "Đã đặt cọc", "Đang bảo trì"));
        dto.setBieuDoPhongValues(Arrays.asList(countDaThue, countTrong, countDaDat, countBaoTri));

        return dto;
    }
}
