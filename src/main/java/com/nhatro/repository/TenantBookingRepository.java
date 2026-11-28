package com.nhatro.repository;

import com.nhatro.entity.DatCho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Truy vấn phiếu đặt chỗ THEO KHÁCH THUÊ cho Cổng Khách thuê (TV3).
 * Tách riêng vì DatChoRepository (TV2) chưa có hàm đếm/liệt kê theo khách. Mọi truy vấn đều ràng buộc khach_id.
 */
@Repository
public interface TenantBookingRepository extends JpaRepository<DatCho, Integer> {

    /** Đếm phiếu của một khách theo trạng thái (dùng cho hạn mức tối đa 2 phiếu DANG_GIU). */
    long countByKhachThueIdAndTrangThai(Integer khachId, String trangThai);

    @Query("SELECT d FROM DatCho d JOIN FETCH d.phongTro "
            + "WHERE d.khachThue.id = :khachId ORDER BY d.ngayDat DESC")
    List<DatCho> findAllByKhach(@Param("khachId") Integer khachId);

    /** Chỉ lấy id phòng của phiếu (không nạp entity) để khóa phòng trước khi đọc trạng thái phiếu. */
    @Query("SELECT d.phongTro.id FROM DatCho d WHERE d.id = :id AND d.khachThue.id = :khachId")
    Optional<Integer> findPhongIdByIdAndKhach(@Param("id") Integer id, @Param("khachId") Integer khachId);

    @Query("SELECT d FROM DatCho d JOIN FETCH d.phongTro WHERE d.id = :id AND d.khachThue.id = :khachId")
    Optional<DatCho> findByIdAndKhach(@Param("id") Integer id, @Param("khachId") Integer khachId);
}
