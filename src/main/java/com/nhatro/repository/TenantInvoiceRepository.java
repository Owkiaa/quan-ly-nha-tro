package com.nhatro.repository;

import com.nhatro.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Truy vấn hóa đơn THEO KHÁCH THUÊ cho Cổng Khách thuê (TV3).
 * Mọi truy vấn đều ràng buộc khach_id để khách không xem được hóa đơn của người khác.
 */
@Repository
public interface TenantInvoiceRepository extends JpaRepository<HoaDon, Integer> {

    @Query(value = "SELECT hd FROM HoaDon hd JOIN FETCH hd.hopDong h JOIN FETCH h.phongTro "
            + "WHERE h.khachThue.id = :khachId "
            + "AND (:trangThai IS NULL OR :trangThai = '' OR hd.trangThai = :trangThai)",
            countQuery = "SELECT COUNT(hd) FROM HoaDon hd "
                    + "WHERE hd.hopDong.khachThue.id = :khachId "
                    + "AND (:trangThai IS NULL OR :trangThai = '' OR hd.trangThai = :trangThai)")
    Page<HoaDon> findByKhach(@Param("khachId") Integer khachId,
                             @Param("trangThai") String trangThai,
                             Pageable pageable);

    @Query("SELECT hd FROM HoaDon hd JOIN FETCH hd.hopDong h JOIN FETCH h.phongTro "
            + "WHERE hd.id = :id AND h.khachThue.id = :khachId")
    Optional<HoaDon> findByIdAndKhach(@Param("id") Integer id, @Param("khachId") Integer khachId);

    @Query("SELECT COUNT(hd) FROM HoaDon hd "
            + "WHERE hd.hopDong.khachThue.id = :khachId AND hd.trangThai = 'CHUA_THANH_TOAN'")
    long countUnpaid(@Param("khachId") Integer khachId);

    @Query("SELECT SUM(hd.tongTien) FROM HoaDon hd "
            + "WHERE hd.hopDong.khachThue.id = :khachId AND hd.trangThai = 'CHUA_THANH_TOAN'")
    BigDecimal sumUnpaid(@Param("khachId") Integer khachId);
}
