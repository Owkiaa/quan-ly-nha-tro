package com.nhatro.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO dữ liệu tổng hợp cho màn hình Thống Kê Doanh Thu & Công Suất Phòng.
 * Phụ trách: Trương Đỗ Minh Quân (MSSV: 2001240390)
 */
public class DashboardDTO {
    private long tongSoPhong;
    private long soPhongDangThue;
    private long soPhongTrong;
    private long soPhongDaDat;
    private long soPhongBaoTri;
    private double tiLeLapDay;

    private BigDecimal tongDoanhThuDaThu;
    private BigDecimal tongNoChuaThu;
    private BigDecimal doanhThuThangNay;
    private long soHoaDonChuaThanhToan;
    private long soHoaDonDaThanhToan;

    private List<String> bieuDoThangLabels;
    private List<BigDecimal> bieuDoDoanhThuValues;
    private List<BigDecimal> bieuDoNoValues;

    private List<String> bieuDoPhongLabels;
    private List<Long> bieuDoPhongValues;

    public DashboardDTO() {
    }

    public long getTongSoPhong() {
        return tongSoPhong;
    }

    public void setTongSoPhong(long tongSoPhong) {
        this.tongSoPhong = tongSoPhong;
    }

    public long getSoPhongDangThue() {
        return soPhongDangThue;
    }

    public void setSoPhongDangThue(long soPhongDangThue) {
        this.soPhongDangThue = soPhongDangThue;
    }

    public long getSoPhongTrong() {
        return soPhongTrong;
    }

    public void setSoPhongTrong(long soPhongTrong) {
        this.soPhongTrong = soPhongTrong;
    }

    public long getSoPhongDaDat() {
        return soPhongDaDat;
    }

    public void setSoPhongDaDat(long soPhongDaDat) {
        this.soPhongDaDat = soPhongDaDat;
    }

    public long getSoPhongBaoTri() {
        return soPhongBaoTri;
    }

    public void setSoPhongBaoTri(long soPhongBaoTri) {
        this.soPhongBaoTri = soPhongBaoTri;
    }

    public double getTiLeLapDay() {
        return tiLeLapDay;
    }

    public void setTiLeLapDay(double tiLeLapDay) {
        this.tiLeLapDay = tiLeLapDay;
    }

    public BigDecimal getTongDoanhThuDaThu() {
        return tongDoanhThuDaThu;
    }

    public void setTongDoanhThuDaThu(BigDecimal tongDoanhThuDaThu) {
        this.tongDoanhThuDaThu = tongDoanhThuDaThu;
    }

    public BigDecimal getTongNoChuaThu() {
        return tongNoChuaThu;
    }

    public void setTongNoChuaThu(BigDecimal tongNoChuaThu) {
        this.tongNoChuaThu = tongNoChuaThu;
    }

    public BigDecimal getDoanhThuThangNay() {
        return doanhThuThangNay;
    }

    public void setDoanhThuThangNay(BigDecimal doanhThuThangNay) {
        this.doanhThuThangNay = doanhThuThangNay;
    }

    public long getSoHoaDonChuaThanhToan() {
        return soHoaDonChuaThanhToan;
    }

    public void setSoHoaDonChuaThanhToan(long soHoaDonChuaThanhToan) {
        this.soHoaDonChuaThanhToan = soHoaDonChuaThanhToan;
    }

    public long getSoHoaDonDaThanhToan() {
        return soHoaDonDaThanhToan;
    }

    public void setSoHoaDonDaThanhToan(long soHoaDonDaThanhToan) {
        this.soHoaDonDaThanhToan = soHoaDonDaThanhToan;
    }

    public List<String> getBieuDoThangLabels() {
        return bieuDoThangLabels;
    }

    public void setBieuDoThangLabels(List<String> bieuDoThangLabels) {
        this.bieuDoThangLabels = bieuDoThangLabels;
    }

    public List<BigDecimal> getBieuDoDoanhThuValues() {
        return bieuDoDoanhThuValues;
    }

    public void setBieuDoDoanhThuValues(List<BigDecimal> bieuDoDoanhThuValues) {
        this.bieuDoDoanhThuValues = bieuDoDoanhThuValues;
    }

    public List<BigDecimal> getBieuDoNoValues() {
        return bieuDoNoValues;
    }

    public void setBieuDoNoValues(List<BigDecimal> bieuDoNoValues) {
        this.bieuDoNoValues = bieuDoNoValues;
    }

    public List<String> getBieuDoPhongLabels() {
        return bieuDoPhongLabels;
    }

    public void setBieuDoPhongLabels(List<String> bieuDoPhongLabels) {
        this.bieuDoPhongLabels = bieuDoPhongLabels;
    }

    public List<Long> getBieuDoPhongValues() {
        return bieuDoPhongValues;
    }

    public void setBieuDoPhongValues(List<Long> bieuDoPhongValues) {
        this.bieuDoPhongValues = bieuDoPhongValues;
    }
}
