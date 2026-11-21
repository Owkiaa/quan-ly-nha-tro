package com.nhatro.dto;

import java.math.BigDecimal;

/**
 * DTO nhận dữ liệu từ form Lập hóa đơn tiền phòng và chốt số điện nước.
 * Viết thủ công không dùng Lombok theo quy chuẩn đồ án.
 */
public class HoaDonCreateDTO {

    private Integer hopDongId;
    private String thangNam; // Định dạng MM/yyyy
    private Integer chiSoDienCu = 0;
    private Integer chiSoDienMoi = 0;
    private BigDecimal donGiaDien = new BigDecimal("2200");
    private Integer chiSoNuocCu = 0;
    private Integer chiSoNuocMoi = 0;
    private BigDecimal donGiaNuoc = new BigDecimal("12900");
    private BigDecimal tienPhong = BigDecimal.ZERO;
    private BigDecimal tienDichVu = new BigDecimal("50000");

    public HoaDonCreateDTO() {
    }

    public HoaDonCreateDTO(Integer hopDongId, String thangNam, Integer chiSoDienCu, Integer chiSoDienMoi, BigDecimal donGiaDien, Integer chiSoNuocCu, Integer chiSoNuocMoi, BigDecimal donGiaNuoc, BigDecimal tienPhong, BigDecimal tienDichVu) {
        this.hopDongId = hopDongId;
        this.thangNam = thangNam;
        this.chiSoDienCu = chiSoDienCu;
        this.chiSoDienMoi = chiSoDienMoi;
        this.donGiaDien = donGiaDien;
        this.chiSoNuocCu = chiSoNuocCu;
        this.chiSoNuocMoi = chiSoNuocMoi;
        this.donGiaNuoc = donGiaNuoc;
        this.tienPhong = tienPhong;
        this.tienDichVu = tienDichVu;
    }

    public Integer getHopDongId() {
        return hopDongId;
    }

    public void setHopDongId(Integer hopDongId) {
        this.hopDongId = hopDongId;
    }

    public String getThangNam() {
        return thangNam;
    }

    public void setThangNam(String thangNam) {
        this.thangNam = thangNam;
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

    public BigDecimal getDonGiaDien() {
        return donGiaDien;
    }

    public void setDonGiaDien(BigDecimal donGiaDien) {
        this.donGiaDien = donGiaDien;
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

    public BigDecimal getDonGiaNuoc() {
        return donGiaNuoc;
    }

    public void setDonGiaNuoc(BigDecimal donGiaNuoc) {
        this.donGiaNuoc = donGiaNuoc;
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
