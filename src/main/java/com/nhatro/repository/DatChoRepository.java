package com.nhatro.repository;

import com.nhatro.entity.DatCho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatChoRepository extends JpaRepository<DatCho, Integer> {

    @Query("SELECT d FROM DatCho d JOIN FETCH d.phongTro JOIN FETCH d.khachThue WHERE d.trangThai = 'DANG_GIU' AND d.hanGiuCho < :now")
    List<DatCho> findExpiredBookings(@Param("now") LocalDateTime now);

    boolean existsByPhongTroIdAndTrangThai(Integer phongId, String trangThai);

    boolean existsByKhachThueIdAndTrangThai(Integer khachId, String trangThai);

    Optional<DatCho> findFirstByPhongTroIdAndTrangThai(Integer phongId, String trangThai);

    long countByTrangThai(String trangThai);

    long countByKhachThueCccdAndTrangThai(String cccd, String trangThai);

    @Query("SELECT d FROM DatCho d JOIN FETCH d.phongTro JOIN FETCH d.khachThue WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(d.phongTro.maPhong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.khachThue.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR d.khachThue.cccd LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR d.trangThai = :trangThai)")
    Page<DatCho> search(@Param("keyword") String keyword, @Param("trangThai") String trangThai, Pageable pageable);
}
