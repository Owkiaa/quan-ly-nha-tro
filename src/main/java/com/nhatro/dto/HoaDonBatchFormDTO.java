package com.nhatro.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HoaDonBatchFormDTO {

    private String thangNam;
    private BigDecimal donGiaDien = new BigDecimal("3500");
    private BigDecimal donGiaNuoc = new BigDecimal("20000");
    private List<HoaDonBatchItemDTO> items = new ArrayList<>();

    public HoaDonBatchFormDTO() {
    }

    public String getThangNam() {
        return thangNam;
    }

    public void setThangNam(String thangNam) {
        this.thangNam = thangNam;
    }

    public BigDecimal getDonGiaDien() {
        return donGiaDien;
    }

    public void setDonGiaDien(BigDecimal donGiaDien) {
        this.donGiaDien = donGiaDien;
    }

    public BigDecimal getDonGiaNuoc() {
        return donGiaNuoc;
    }

    public void setDonGiaNuoc(BigDecimal donGiaNuoc) {
        this.donGiaNuoc = donGiaNuoc;
    }

    public List<HoaDonBatchItemDTO> getItems() {
        return items;
    }

    public void setItems(List<HoaDonBatchItemDTO> items) {
        this.items = items;
    }
}
