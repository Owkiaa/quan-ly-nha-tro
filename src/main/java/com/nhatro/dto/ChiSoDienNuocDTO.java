package com.nhatro.dto;

import java.math.BigDecimal;

public class ChiSoDienNuocDTO {

    private Integer hopDongId;
    private String maHopDong;
    private String maPhong;
    private String tenKhachThue;
    private BigDecimal giaThue;
    private Integer chiSoDienCu;
    private Integer chiSoNuocCu;

    public ChiSoDienNuocDTO() {
    }

    public ChiSoDienNuocDTO(Integer hopDongId, String maHopDong, String maPhong, String tenKhachThue, BigDecimal giaThue, Integer chiSoDienCu, Integer chiSoNuocCu) {
        this.hopDongId = hopDongId;
        this.maHopDong = maHopDong;
        this.maPhong = maPhong;
        this.tenKhachThue = tenKhachThue;
        this.giaThue = giaThue;
        this.chiSoDienCu = chiSoDienCu;
        this.chiSoNuocCu = chiSoNuocCu;
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

    public BigDecimal getGiaThue() {
        return giaThue;
    }

    public void setGiaThue(BigDecimal giaThue) {
        this.giaThue = giaThue;
    }

    public Integer getChiSoDienCu() {
        return chiSoDienCu;
    }

    public void setChiSoDienCu(Integer chiSoDienCu) {
        this.chiSoDienCu = chiSoDienCu;
    }

    public Integer getChiSoNuocCu() {
        return chiSoNuocCu;
    }

    public void setChiSoNuocCu(Integer chiSoNuocCu) {
        this.chiSoNuocCu = chiSoNuocCu;
    }
}
