package com.nhatro.dto;

import com.nhatro.entity.DatCho;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object hiển thị thông tin đặt giữ chỗ phòng trọ.
 */
public class DatChoDTO {

    private Integer id;
    private Integer phongId;
    private String maPhong;
    private String tenPhong;
    private BigDecimal giaThue;
    private Integer khachId;
    private String hoTenKhach;
    private String cccdKhach;
    private String soDienThoaiKhach;
    private LocalDateTime ngayDat;
    private LocalDateTime hanGiuCho;
    private String trangThai; // DANG_GIU, HET_HAN, DA_CHUYEN_HOP_DONG
    private String ghiChu;

    public DatChoDTO() {
    }

    public DatChoDTO(Integer id, Integer phongId, String maPhong, String tenPhong, BigDecimal giaThue,
                     Integer khachId, String hoTenKhach, String cccdKhach, String soDienThoaiKhach,
                     LocalDateTime ngayDat, LocalDateTime hanGiuCho, String trangThai, String ghiChu) {
        this.id = id;
        this.phongId = phongId;
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.giaThue = giaThue;
        this.khachId = khachId;
        this.hoTenKhach = hoTenKhach;
        this.cccdKhach = cccdKhach;
        this.soDienThoaiKhach = soDienThoaiKhach;
        this.ngayDat = ngayDat;
        this.hanGiuCho = hanGiuCho;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public static DatChoDTO fromEntity(DatCho entity) {
        if (entity == null) {
            return null;
        }
        DatChoDTO dto = new DatChoDTO();
        dto.setId(entity.getId());
        if (entity.getPhongTro() != null) {
            dto.setPhongId(entity.getPhongTro().getId());
            dto.setMaPhong(entity.getPhongTro().getMaPhong());
            dto.setTenPhong(entity.getPhongTro().getTenPhong());
            dto.setGiaThue(entity.getPhongTro().getGiaThue());
        }
        if (entity.getKhachThue() != null) {
            dto.setKhachId(entity.getKhachThue().getId());
            dto.setHoTenKhach(entity.getKhachThue().getHoTen());
            dto.setCccdKhach(entity.getKhachThue().getCccd());
            dto.setSoDienThoaiKhach(entity.getKhachThue().getSoDienThoai());
        }
        dto.setNgayDat(entity.getNgayDat());
        dto.setHanGiuCho(entity.getHanGiuCho());
        dto.setTrangThai(entity.getTrangThai());
        dto.setGhiChu(entity.getGhiChu());
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public BigDecimal getGiaThue() {
        return giaThue;
    }

    public void setGiaThue(BigDecimal giaThue) {
        this.giaThue = giaThue;
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

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }

    public LocalDateTime getHanGiuCho() {
        return hanGiuCho;
    }

    public void setHanGiuCho(LocalDateTime hanGiuCho) {
        this.hanGiuCho = hanGiuCho;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
