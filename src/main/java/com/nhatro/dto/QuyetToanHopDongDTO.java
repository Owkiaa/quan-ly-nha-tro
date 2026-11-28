package com.nhatro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object phục vụ Bảng quyết toán tiền cọc khi thanh lý hợp đồng.
 * Giúp minh bạch tài chính: Tiền cọc ban đầu - Nợ hóa đơn - Chi phí khấu trừ = Tiền thực hoàn.
 * 
 * Phụ trách bởi: Bùi Trần Tấn Tài
 */
public class QuyetToanHopDongDTO {

    private Integer hopDongId;
    private String maHopDong;
    private String maPhong;
    private String tenKhachThue;
    private BigDecimal tienDatCocBanDau = BigDecimal.ZERO;
    private BigDecimal tongNoHoaDon = BigDecimal.ZERO;
    private Integer soHoaDonChuaThanhToan = 0;
    private BigDecimal chiPhiKhauTru = BigDecimal.ZERO;
    private String lyDoKhauTru;
    private BigDecimal tienThucHoan = BigDecimal.ZERO;
    private Boolean gachNoHoaDon = Boolean.TRUE;
    private LocalDate ngayQuyetToan = LocalDate.now();

    public QuyetToanHopDongDTO() {
    }

    public QuyetToanHopDongDTO(Integer hopDongId, String maHopDong, String maPhong, String tenKhachThue,
                               BigDecimal tienDatCocBanDau, BigDecimal tongNoHoaDon, Integer soHoaDonChuaThanhToan,
                               BigDecimal chiPhiKhauTru, String lyDoKhauTru, BigDecimal tienThucHoan,
                               Boolean gachNoHoaDon, LocalDate ngayQuyetToan) {
        this.hopDongId = hopDongId;
        this.maHopDong = maHopDong;
        this.maPhong = maPhong;
        this.tenKhachThue = tenKhachThue;
        this.tienDatCocBanDau = tienDatCocBanDau != null ? tienDatCocBanDau : BigDecimal.ZERO;
        this.tongNoHoaDon = tongNoHoaDon != null ? tongNoHoaDon : BigDecimal.ZERO;
        this.soHoaDonChuaThanhToan = soHoaDonChuaThanhToan != null ? soHoaDonChuaThanhToan : 0;
        this.chiPhiKhauTru = chiPhiKhauTru != null ? chiPhiKhauTru : BigDecimal.ZERO;
        this.lyDoKhauTru = lyDoKhauTru;
        this.tienThucHoan = tienThucHoan != null ? tienThucHoan : BigDecimal.ZERO;
        this.gachNoHoaDon = gachNoHoaDon != null ? gachNoHoaDon : Boolean.TRUE;
        this.ngayQuyetToan = ngayQuyetToan != null ? ngayQuyetToan : LocalDate.now();
    }

    public Integer getHopDongId() {
        return hopDongId;
    }

    public void setHopDongId(Integer hopDongId) {
        this.hopDongId = hopDongId;
    }

    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenKhachThue() {
        return tenKhachThue;
    }

    public void setTenKhachThue(String tenKhachThue) {
        this.tenKhachThue = tenKhachThue;
    }

    public BigDecimal getTienDatCocBanDau() {
        return tienDatCocBanDau;
    }

    public void setTienDatCocBanDau(BigDecimal tienDatCocBanDau) {
        this.tienDatCocBanDau = tienDatCocBanDau;
    }

    public BigDecimal getTongNoHoaDon() {
        return tongNoHoaDon;
    }

    public void setTongNoHoaDon(BigDecimal tongNoHoaDon) {
        this.tongNoHoaDon = tongNoHoaDon;
    }

    public Integer getSoHoaDonChuaThanhToan() {
        return soHoaDonChuaThanhToan;
    }

    public void setSoHoaDonChuaThanhToan(Integer soHoaDonChuaThanhToan) {
        this.soHoaDonChuaThanhToan = soHoaDonChuaThanhToan;
    }

    public BigDecimal getChiPhiKhauTru() {
        return chiPhiKhauTru;
    }

    public void setChiPhiKhauTru(BigDecimal chiPhiKhauTru) {
        this.chiPhiKhauTru = chiPhiKhauTru;
    }

    public String getLyDoKhauTru() {
        return lyDoKhauTru;
    }

    public void setLyDoKhauTru(String lyDoKhauTru) {
        this.lyDoKhauTru = lyDoKhauTru;
    }

    public BigDecimal getTienThucHoan() {
        return tienThucHoan;
    }

    public void setTienThucHoan(BigDecimal tienThucHoan) {
        this.tienThucHoan = tienThucHoan;
    }

    public Boolean getGachNoHoaDon() {
        return gachNoHoaDon;
    }

    public void setGachNoHoaDon(Boolean gachNoHoaDon) {
        this.gachNoHoaDon = gachNoHoaDon;
    }

    public LocalDate getNgayQuyetToan() {
        return ngayQuyetToan;
    }

    public void setNgayQuyetToan(LocalDate ngayQuyetToan) {
        this.ngayQuyetToan = ngayQuyetToan;
    }
}
