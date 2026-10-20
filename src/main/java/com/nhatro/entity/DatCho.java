package com.nhatro.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dat_cho")
public class DatCho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id", nullable = false)
    private PhongTro phongTro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_id", nullable = false)
    private KhachThue khachThue;

    @Column(name = "ngay_dat", nullable = false)
    private LocalDateTime ngayDat = LocalDateTime.now();

    @Column(name = "han_giu_cho", nullable = false)
    private LocalDateTime hanGiuCho;

    @Column(name = "trang_thai", nullable = false, length = 30)
    private String trangThai; // DANG_GIU, HET_HAN, DA_CHUYEN_HOP_DONG

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    public DatCho() {
    }

    public DatCho(PhongTro phongTro, KhachThue khachThue, LocalDateTime ngayDat, LocalDateTime hanGiuCho, String trangThai, String ghiChu) {
        this.phongTro = phongTro;
        this.khachThue = khachThue;
        this.ngayDat = ngayDat;
        this.hanGiuCho = hanGiuCho;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
