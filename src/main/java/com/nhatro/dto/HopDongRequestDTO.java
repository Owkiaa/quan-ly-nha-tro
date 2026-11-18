package com.nhatro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object nhận dữ liệu yêu cầu lập/cập nhật hợp đồng thuê phòng.
 */
public class HopDongRequestDTO {

    private Integer id;
    private String maHopDong;
    private Integer phongId;
    private Integer khachId;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private BigDecimal tienDatCoc;
    private BigDecimal giaThueThoaThuan;
    private Integer chuKyThanhToan = 1;
    private String trangThai; // HIEU_LUC, HET_HAN, DA_THANH_LY

    public HopDongRequestDTO() {
    }

    public HopDongRequestDTO(Integer id, String maHopDong, Integer phongId, Integer khachId,
                             LocalDate ngayBatDau, LocalDate ngayKetThuc, BigDecimal tienDatCoc,
                             BigDecimal giaThueThoaThuan, Integer chuKyThanhToan, String trangThai) {
        this.id = id;
        this.maHopDong = maHopDong;
        this.phongId = phongId;
        this.khachId = khachId;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.tienDatCoc = tienDatCoc;
        this.giaThueThoaThuan = giaThueThoaThuan;
        this.chuKyThanhToan = (chuKyThanhToan != null && chuKyThanhToan > 0) ? chuKyThanhToan : 1;
        this.trangThai = trangThai;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    public Integer getPhongId() {
        return phongId;
    }

    public void setPhongId(Integer phongId) {
        this.phongId = phongId;
    }

    public Integer getKhachId() {
        return khachId;
    }

    public void setKhachId(Integer khachId) {
        this.khachId = khachId;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public BigDecimal getTienDatCoc() {
        return tienDatCoc;
    }

    public void setTienDatCoc(BigDecimal tienDatCoc) {
        this.tienDatCoc = tienDatCoc;
    }

    public BigDecimal getGiaThueThoaThuan() {
        return giaThueThoaThuan;
    }

    public void setGiaThueThoaThuan(BigDecimal giaThueThoaThuan) {
        this.giaThueThoaThuan = giaThueThoaThuan;
    }

    public Integer getChuKyThanhToan() {
        return chuKyThanhToan;
    }

    public void setChuKyThanhToan(Integer chuKyThanhToan) {
        this.chuKyThanhToan = chuKyThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
