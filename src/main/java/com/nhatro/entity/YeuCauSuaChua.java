package com.nhatro.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "yeu_cau_sua_chua")
public class YeuCauSuaChua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id", nullable = false)
    private PhongTro phongTro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_id", nullable = false)
    private KhachThue khachThue;

    @Column(name = "noi_dung", nullable = false, length = 255)
    private String noiDung;

    @Column(name = "tho_sua_chua", length = 100)
    private String thoSuaChua;

    @Column(name = "chi_phi", precision = 12, scale = 2)
    private BigDecimal chiPhi = BigDecimal.ZERO;

    @Column(name = "muc_do", nullable = false, length = 20)
    private String mucDo; // BINH_THUONG, CAP_BACH

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai; // CHO_TIEP_NHAN, DANG_SUA, HOAN_THANH

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_hoan_thanh")
    private LocalDateTime ngayHoanThanh;

    public YeuCauSuaChua() {
    }

    public YeuCauSuaChua(PhongTro phongTro, KhachThue khachThue, String noiDung, String thoSuaChua, BigDecimal chiPhi, String mucDo, String trangThai, LocalDateTime ngayTao, LocalDateTime ngayHoanThanh) {
        this.phongTro = phongTro;
        this.khachThue = khachThue;
        this.noiDung = noiDung;
        this.thoSuaChua = thoSuaChua;
        this.chiPhi = chiPhi;
        this.mucDo = mucDo;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ngayHoanThanh = ngayHoanThanh;
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

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getThoSuaChua() {
        return thoSuaChua;
    }

    public void setThoSuaChua(String thoSuaChua) {
        this.thoSuaChua = thoSuaChua;
    }

    public BigDecimal getChiPhi() {
        return chiPhi;
    }

    public void setChiPhi(BigDecimal chiPhi) {
        this.chiPhi = chiPhi;
    }

    public String getMucDo() {
        return mucDo;
    }

    public void setMucDo(String mucDo) {
        this.mucDo = mucDo;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayHoanThanh() {
        return ngayHoanThanh;
    }

    public void setNgayHoanThanh(LocalDateTime ngayHoanThanh) {
        this.ngayHoanThanh = ngayHoanThanh;
    }
}
