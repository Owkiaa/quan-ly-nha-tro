package com.nhatro.service;

import com.nhatro.entity.KhachThue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface KhachThueService {

    Page<KhachThue> search(String keyword, String trangThai, Pageable pageable);

    List<KhachThue> getAll();

    KhachThue getById(Integer id);

    KhachThue getByCccd(String cccd);

    KhachThue create(KhachThue khachThue);

    KhachThue update(Integer id, KhachThue khachThue);

    void delete(Integer id);

    long countByTrangThai(String trangThai);

    long countAll();
}
