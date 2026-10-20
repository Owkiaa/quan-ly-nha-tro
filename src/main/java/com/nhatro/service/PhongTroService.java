package com.nhatro.service;

import com.nhatro.entity.PhongTro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhongTroService {

    Page<PhongTro> search(String keyword, String trangThai, Pageable pageable);

    List<PhongTro> getAll();

    PhongTro getById(Integer id);

    PhongTro getByMaPhong(String maPhong);

    PhongTro create(PhongTro phongTro, MultipartFile imageFile);

    PhongTro update(Integer id, PhongTro phongTro, MultipartFile imageFile);

    void delete(Integer id);

    long countByTrangThai(String trangThai);

    long countAll();
}
