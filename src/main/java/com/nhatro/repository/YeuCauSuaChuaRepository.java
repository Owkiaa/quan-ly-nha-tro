package com.nhatro.repository;

import com.nhatro.entity.YeuCauSuaChua;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface YeuCauSuaChuaRepository extends JpaRepository<YeuCauSuaChua, Integer> {

    boolean existsByPhongTroIdAndTrangThaiNot(Integer phongId, String trangThai);

    boolean existsByKhachThueIdAndTrangThaiNot(Integer khachId, String trangThai);

    @Query(value = "SELECT y FROM YeuCauSuaChua y JOIN FETCH y.phongTro JOIN FETCH y.khachThue WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(y.phongTro.maPhong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(y.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR y.trangThai = :trangThai)",
           countQuery = "SELECT COUNT(y) FROM YeuCauSuaChua y WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(y.phongTro.maPhong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(y.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR y.trangThai = :trangThai)")
    Page<YeuCauSuaChua> search(@Param("keyword") String keyword, @Param("trangThai") String trangThai, Pageable pageable);
}
