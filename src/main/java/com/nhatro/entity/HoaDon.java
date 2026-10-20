package com.nhatro.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "hoa_don",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_hoa_don_hop_dong_thang", columnNames = {"hop_dong_id", "thang_nam"})
    }
)
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_hoa_don", nullable = false, unique = true, length = 30)
    private String maHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hop_dong_id", nullable = false)
    private HopDong hopDong;

    @Column(name = "thang_nam", nullable = false, length = 7)
    private String thangNam; // MM/yyyy

    @Column(name = "chi_so_dien_cu", nullable = false)
    private Integer chiSoDienCu;

    @Column(name = "chi_so_dien_moi", nullable = false)
    private Integer chiSoDienMoi;

    @Column(name = "don_gia_dien", nullable = false, precision = 10, scale = 2)
    private BigDecimal donGiaDien = new BigDecimal("3500");

    @Column(name = "chi_so_nuoc_cu", nullable = false)
    private Integer chiSoNuocCu;

    @Column(name = "chi_so_nuoc_moi", nullable = false)
    private Integer chiSoNuocMoi;

    @Column(name = "don_gia_nuoc", nullable = false, precision = 10, scale = 2)
    private BigDecimal donGiaNuoc = new BigDecimal("20000");

    @Column(name = "tien_phong", nullable = false, precision = 12, scale = 2)
    private BigDecimal tienPhong;

    @Column(name = "tien_dien", nullable = false, precision = 12, scale = 2)
    private BigDecimal tienDien;

    @Column(name = "tien_nuoc", nullable = false, precision = 12, scale = 2)
    private BigDecimal tienNuoc;

    @Column(name = "tien_dich_vu", nullable = false, precision = 12, scale = 2)
    private BigDecimal tienDichVu = new BigDecimal("100000");

    @Column(name = "tong_tien", nullable = false, precision = 12, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai; // CHUA_THANH_TOAN, DA_THANH_TOAN

    @Column(name = "ngay_lap", nullable = false)
    private LocalDateTime ngayLap = LocalDateTime.now();

    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, HopDong hopDong, String thangNam, Integer chiSoDienCu, Integer chiSoDienMoi, BigDecimal donGiaDien, Integer chiSoNuocCu, Integer chiSoNuocMoi, BigDecimal donGiaNuoc, BigDecimal tienPhong, BigDecimal tienDien, BigDecimal tienNuoc, BigDecimal tienDichVu, BigDecimal tongTien, String trangThai, LocalDateTime ngayLap, LocalDateTime ngayThanhToan) {
        this.maHoaDon = maHoaDon;
        this.hopDong = hopDong;
        this.thangNam = thangNam;
        this.chiSoDienCu = chiSoDienCu;
        this.chiSoDienMoi = chiSoDienMoi;
        this.donGiaDien = donGiaDien;
        this.chiSoNuocCu = chiSoNuocCu;
        this.chiSoNuocMoi = chiSoNuocMoi;
        this.donGiaNuoc = donGiaNuoc;
        this.tienPhong = tienPhong;
        this.tienDien = tienDien;
        this.tienNuoc = tienNuoc;
        this.tienDichVu = tienDichVu;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.ngayLap = ngayLap;
        this.ngayThanhToan = ngayThanhToan;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public HopDong getHopDong() {
        return hopDong;
    }

    public void setHopDong(HopDong hopDong) {
        this.hopDong = hopDong;
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

    public BigDecimal getTienDien() {
        return tienDien;
    }

    public void setTienDien(BigDecimal tienDien) {
        this.tienDien = tienDien;
    }

    public BigDecimal getTienNuoc() {
        return tienNuoc;
    }

    public void setTienNuoc(BigDecimal tienNuoc) {
        this.tienNuoc = tienNuoc;
    }

    public BigDecimal getTienDichVu() {
        return tienDichVu;
    }

    public void setTienDichVu(BigDecimal tienDichVu) {
        this.tienDichVu = tienDichVu;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public LocalDateTime getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(LocalDateTime ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }
}
