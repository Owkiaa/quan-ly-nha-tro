package com.nhatro.dto;

import java.math.BigDecimal;

public class HoaDonBatchItemDTO {

    private boolean selected;
    private Integer hopDongId;
    private String maHopDong;
    private String maPhong;
    private String tenKhachThue;
    private boolean daCoHoaDon;
    private Integer existingHoaDonId;
    private String existingMaHoaDon;
    private Integer chiSoDienCu = 0;
    private Integer chiSoDienMoi = 0;
    private Integer chiSoNuocCu = 0;
    private Integer chiSoNuocMoi = 0;
    private BigDecimal tienPhong = BigDecimal.ZERO;
    private BigDecimal tienDichVu = new BigDecimal("100000");

    public HoaDonBatchItemDTO() {
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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

    public boolean isDaCoHoaDon() {
        return daCoHoaDon;
    }

    public void setDaCoHoaDon(boolean daCoHoaDon) {
        this.daCoHoaDon = daCoHoaDon;
    }

    public Integer getExistingHoaDonId() {
        return existingHoaDonId;
    }

    public void setExistingHoaDonId(Integer existingHoaDonId) {
        this.existingHoaDonId = existingHoaDonId;
    }

    public String getExistingMaHoaDon() {
        return existingMaHoaDon;
    }

    public void setExistingMaHoaDon(String existingMaHoaDon) {
        this.existingMaHoaDon = existingMaHoaDon;
    }

    public Integer getChiSoDienCu() {
        return chiSoDienCu;
    }

    public void setChiSoDienCu(Integer chiSoDienCu) {
        this.chiSoDienCu = chiSoDienCu;
    }

    public Integer getChiSoDienMoi() {
        return chiSoDienMoi;
    }

    public void setChiSoDienMoi(Integer chiSoDienMoi) {
        this.chiSoDienMoi = chiSoDienMoi;
    }

    public Integer getChiSoNuocCu() {
        return chiSoNuocCu;
    }

    public void setChiSoNuocCu(Integer chiSoNuocCu) {
        this.chiSoNuocCu = chiSoNuocCu;
    }

    public Integer getChiSoNuocMoi() {
        return chiSoNuocMoi;
    }

    public void setChiSoNuocMoi(Integer chiSoNuocMoi) {
        this.chiSoNuocMoi = chiSoNuocMoi;
    }

    public BigDecimal getTienPhong() {
        return tienPhong;
    }

    public void setTienPhong(BigDecimal tienPhong) {
        this.tienPhong = tienPhong;
    }

    public BigDecimal getTienDichVu() {
        return tienDichVu;
    }

    public void setTienDichVu(BigDecimal tienDichVu) {
        this.tienDichVu = tienDichVu;
    }
}
