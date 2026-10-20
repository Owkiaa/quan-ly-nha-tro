package com.nhatro.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "phong_tro")
public class PhongTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phong", nullable = false, unique = true, length = 20)
    private String maPhong;

    @Column(name = "ten_phong", nullable = false, length = 50)
    private String tenPhong;

    @Column(name = "loai_phong", length = 30)
    private String loaiPhong;

    @Column(name = "dien_tich")
    private Double dienTich;

    @Column(name = "gia_thue", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaThue;

    @Column(name = "hinh_anh", length = 255)
    private String hinhAnh;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai; // TRONG, DA_DAT, DA_THUE, BAO_TRI

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @OneToMany(mappedBy = "phongTro", fetch = FetchType.LAZY)
    private List<HopDong> danhSachHopDong = new ArrayList<>();

    public PhongTro() {
    }

    public PhongTro(String maPhong, String tenPhong, String loaiPhong, Double dienTich, BigDecimal giaThue, String hinhAnh, String trangThai, String moTa) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.loaiPhong = loaiPhong;
        this.dienTich = dienTich;
        this.giaThue = giaThue;
        this.hinhAnh = hinhAnh;
        this.trangThai = trangThai;
        this.moTa = moTa;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public Double getDienTich() {
        return dienTich;
    }

    public void setDienTich(Double dienTich) {
        this.dienTich = dienTich;
    }

    public BigDecimal getGiaThue() {
        return giaThue;
    }

    public void setGiaThue(BigDecimal giaThue) {
        this.giaThue = giaThue;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public List<HopDong> getDanhSachHopDong() {
        return danhSachHopDong;
    }

    public void setDanhSachHopDong(List<HopDong> danhSachHopDong) {
        this.danhSachHopDong = danhSachHopDong;
    }
}
