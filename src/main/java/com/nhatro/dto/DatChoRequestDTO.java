package com.nhatro.dto;

/**
 * Data Transfer Object nhận dữ liệu yêu cầu đặt giữ chỗ từ Web Form hoặc API.
 */
public class DatChoRequestDTO {

    private Integer phongId;
    private String maPhong;
    private String hoTen;
    private String cccd;
    private String soDienThoai;
    private String email;
    private Integer soGioGiuCho = 24;
    private String ghiChu;

    public DatChoRequestDTO() {
    }

    public DatChoRequestDTO(Integer phongId, String maPhong, String hoTen, String cccd,
                           String soDienThoai, String email, Integer soGioGiuCho, String ghiChu) {
        this.phongId = phongId;
        this.maPhong = maPhong;
        this.hoTen = hoTen;
        this.cccd = cccd;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.soGioGiuCho = (soGioGiuCho != null && soGioGiuCho > 0) ? soGioGiuCho : 24;
        this.ghiChu = ghiChu;
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

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSoGioGiuCho() {
        return soGioGiuCho;
    }

    public void setSoGioGiuCho(Integer soGioGiuCho) {
        this.soGioGiuCho = soGioGiuCho;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
