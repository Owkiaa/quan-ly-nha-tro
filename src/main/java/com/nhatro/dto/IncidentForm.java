package com.nhatro.dto;

/** Dữ liệu form "Gửi phiếu phản ánh sự cố" của khách thuê. */
public class IncidentForm {

    private Integer phongId;
    private String noiDung;
    private String mucDo = "BINH_THUONG";

    public IncidentForm() {
    }

    public Integer getPhongId() {
        return phongId;
    }

    public void setPhongId(Integer phongId) {
        this.phongId = phongId;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getMucDo() {
        return mucDo;
    }

    public void setMucDo(String mucDo) {
        this.mucDo = mucDo;
    }
}
