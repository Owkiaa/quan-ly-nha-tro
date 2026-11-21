package com.nhatro.repository;

import com.nhatro.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    boolean existsByMaHoaDon(String maHoaDon);

    boolean existsByHopDongId(Integer hopDongId);

    boolean existsByHopDongIdAndThangNam(Integer hopDongId, String thangNam);

    @Query(value = "SELECT hd FROM HoaDon hd JOIN FETCH hd.hopDong h JOIN FETCH h.phongTro JOIN FETCH h.khachThue WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(hd.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(h.maHopDong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(h.phongTro.maPhong) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:thangNam IS NULL OR :thangNam = '' OR hd.thangNam = :thangNam) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR hd.trangThai = :trangThai)",
           countQuery = "SELECT COUNT(hd) FROM HoaDon hd WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(hd.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(hd.hopDong.maHopDong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(hd.hopDong.phongTro.maPhong) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:thangNam IS NULL OR :thangNam = '' OR hd.thangNam = :thangNam) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR hd.trangThai = :trangThai)")
    Page<HoaDon> search(@Param("keyword") String keyword, @Param("thangNam") String thangNam, @Param("trangThai") String trangThai, Pageable pageable);

    java.util.Optional<HoaDon> findTopByHopDongIdOrderByNgayLapDesc(Integer hopDongId);

    @Query("SELECT COALESCE(SUM(hd.tongTien), 0) FROM HoaDon hd WHERE hd.trangThai = 'DA_THANH_TOAN'")
    java.math.BigDecimal sumTongDoanhThuDaThu();

    @Query("SELECT COALESCE(SUM(hd.tongTien), 0) FROM HoaDon hd WHERE hd.trangThai = 'CHUA_THANH_TOAN'")
    java.math.BigDecimal sumTongNoChuaThu();

    @Query("SELECT COALESCE(SUM(hd.tongTien), 0) FROM HoaDon hd WHERE hd.thangNam = :thangNam AND hd.trangThai = 'DA_THANH_TOAN'")
    java.math.BigDecimal sumDoanhThuTheoThang(@Param("thangNam") String thangNam);

    @Query("SELECT COALESCE(SUM(hd.tongTien), 0) FROM HoaDon hd WHERE hd.thangNam = :thangNam AND hd.trangThai = 'CHUA_THANH_TOAN'")
    java.math.BigDecimal sumNoTheoThang(@Param("thangNam") String thangNam);

    long countByTrangThai(String trangThai);
}
