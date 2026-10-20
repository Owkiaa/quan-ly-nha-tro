package com.nhatro.repository;

import com.nhatro.entity.HopDong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HopDongRepository extends JpaRepository<HopDong, Integer> {

    Optional<HopDong> findByMaHopDong(String maHopDong);

    boolean existsByMaHopDong(String maHopDong);

    boolean existsByPhongTroId(Integer phongId);

    boolean existsByKhachThueId(Integer khachId);

    boolean existsByPhongTroIdAndTrangThai(Integer phongId, String trangThai);

    boolean existsByKhachThueIdAndTrangThai(Integer khachId, String trangThai);

    @Query(value = "SELECT h FROM HopDong h JOIN FETCH h.phongTro JOIN FETCH h.khachThue WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(h.maHopDong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(h.phongTro.maPhong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(h.khachThue.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR h.trangThai = :trangThai)",
           countQuery = "SELECT COUNT(h) FROM HopDong h WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(h.maHopDong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(h.phongTro.maPhong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(h.khachThue.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR h.trangThai = :trangThai)")
    Page<HopDong> search(@Param("keyword") String keyword, @Param("trangThai") String trangThai, Pageable pageable);
}
