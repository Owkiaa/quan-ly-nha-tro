package com.nhatro.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hop_dong")
public class HopDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_hop_dong", nullable = false, unique = true, length = 30)
    private String maHopDong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id", nullable = false)
    private PhongTro phongTro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_id", nullable = false)
    private KhachThue khachThue;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate ngayKetThuc;

    @Column(name = "tien_dat_coc", nullable = false, precision = 12, scale = 2)
    private BigDecimal tienDatCoc;

    @Column(name = "gia_thue_thoa_thuan", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaThueThoaThuan;

    @Column(name = "chu_ky_thanh_toan", nullable = false)
    private Integer chuKyThanhToan = 1;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai; // HIEU_LUC, HET_HAN, DA_THANH_LY

    @OneToMany(mappedBy = "hopDong", fetch = FetchType.LAZY)
    private List<HoaDon> danhSachHoaDon = new ArrayList<>();

    public HopDong() {
    }

    public HopDong(String maHopDong, PhongTro phongTro, KhachThue khachThue, LocalDate ngayBatDau, LocalDate ngayKetThuc, BigDecimal tienDatCoc, BigDecimal giaThueThoaThuan, Integer chuKyThanhToan, String trangThai) {
        this.maHopDong = maHopDong;
        this.phongTro = phongTro;
        this.khachThue = khachThue;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.tienDatCoc = tienDatCoc;
        this.giaThueThoaThuan = giaThueThoaThuan;
        this.chuKyThanhToan = chuKyThanhToan;
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

    public PhongTro getPhongTro() {
        return phongTro;
    }

    public void setPhongTro(PhongTro phongTro) {
        this.phongTro = phongTro;
    }

    public KhachThue getKhachThue() {
        return khachThue;
    }

    public void setKhachThue(KhachThue khachThue) {
        this.khachThue = khachThue;
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

    public List<HoaDon> getDanhSachHoaDon() {
        return danhSachHoaDon;
    }

    public void setDanhSachHoaDon(List<HoaDon> danhSachHoaDon) {
        this.danhSachHoaDon = danhSachHoaDon;
    }
}
