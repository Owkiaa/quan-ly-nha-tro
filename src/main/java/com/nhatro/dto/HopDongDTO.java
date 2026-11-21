package com.nhatro.dto;

import com.nhatro.entity.HopDong;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object hiển thị thông tin hợp đồng thuê phòng.
 */
public class HopDongDTO {

    private Integer id;
    private String maHopDong;
    private Integer phongId;
    private String maPhong;
    private String tenPhong;
    private Integer khachId;
    private String hoTenKhach;
    private String cccdKhach;
    private String soDienThoaiKhach;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private BigDecimal tienDatCoc;
    private BigDecimal giaThueThoaThuan;
    private Integer chuKyThanhToan;
    private String trangThai; // HIEU_LUC, HET_HAN, DA_THANH_LY
    private Integer soLuongHoaDon;

    public HopDongDTO() {
    }

    public HopDongDTO(Integer id, String maHopDong, Integer phongId, String maPhong, String tenPhong,
                      Integer khachId, String hoTenKhach, String cccdKhach, String soDienThoaiKhach,
                      LocalDate ngayBatDau, LocalDate ngayKetThuc, BigDecimal tienDatCoc,
                      BigDecimal giaThueThoaThuan, Integer chuKyThanhToan, String trangThai, Integer soLuongHoaDon) {
        this.id = id;
        this.maHopDong = maHopDong;
        this.phongId = phongId;
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.khachId = khachId;
        this.hoTenKhach = hoTenKhach;
        this.cccdKhach = cccdKhach;
        this.soDienThoaiKhach = soDienThoaiKhach;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.tienDatCoc = tienDatCoc;
        this.giaThueThoaThuan = giaThueThoaThuan;
        this.chuKyThanhToan = chuKyThanhToan;
        this.trangThai = trangThai;
        this.soLuongHoaDon = soLuongHoaDon;
    }

    public static HopDongDTO fromEntity(HopDong entity) {
        if (entity == null) {
            return null;
        }
        HopDongDTO dto = new HopDongDTO();
        dto.setId(entity.getId());
        dto.setMaHopDong(entity.getMaHopDong());
        if (entity.getPhongTro() != null) {
            dto.setPhongId(entity.getPhongTro().getId());
            dto.setMaPhong(entity.getPhongTro().getMaPhong());
            dto.setTenPhong(entity.getPhongTro().getTenPhong());
        }
        if (entity.getKhachThue() != null) {
            dto.setKhachId(entity.getKhachThue().getId());
            dto.setHoTenKhach(entity.getKhachThue().getHoTen());
            dto.setCccdKhach(entity.getKhachThue().getCccd());
            dto.setSoDienThoaiKhach(entity.getKhachThue().getSoDienThoai());
        }
        dto.setNgayBatDau(entity.getNgayBatDau());
        dto.setNgayKetThuc(entity.getNgayKetThuc());
        dto.setTienDatCoc(entity.getTienDatCoc());
        dto.setGiaThueThoaThuan(entity.getGiaThueThoaThuan());
        dto.setChuKyThanhToan(entity.getChuKyThanhToan());
        dto.setTrangThai(entity.getTrangThai());
        if (entity.getDanhSachHoaDon() != null) {
            dto.setSoLuongHoaDon(entity.getDanhSachHoaDon().size());
        } else {
            dto.setSoLuongHoaDon(0);
        }
        return dto;
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

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public Integer getKhachId() {
        return khachId;
    }

    public void setKhachId(Integer khachId) {
        this.khachId = khachId;
    }

    public String getHoTenKhach() {
        return hoTenKhach;
    }

    public void setHoTenKhach(String hoTenKhach) {
        this.hoTenKhach = hoTenKhach;
    }

    public String getCccdKhach() {
        return cccdKhach;
    }

    public void setCccdKhach(String cccdKhach) {
        this.cccdKhach = cccdKhach;
    }

    public String getSoDienThoaiKhach() {
        return soDienThoaiKhach;
    }

    public void setSoDienThoaiKhach(String soDienThoaiKhach) {
        this.soDienThoaiKhach = soDienThoaiKhach;
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

    public Integer getSoLuongHoaDon() {
        return soLuongHoaDon;
    }

    public void setSoLuongHoaDon(Integer soLuongHoaDon) {
        this.soLuongHoaDon = soLuongHoaDon;
    }
}
