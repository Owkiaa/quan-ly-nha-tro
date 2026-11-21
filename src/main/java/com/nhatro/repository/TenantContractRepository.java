package com.nhatro.repository;

import com.nhatro.entity.HopDong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Truy vấn hợp đồng THEO KHÁCH THUÊ cho Cổng Khách thuê (TV3).
 * Tách riêng để không sửa HopDongRepository của TV2. Có thể gộp vào HopDongRepository sau.
 */
@Repository
public interface TenantContractRepository extends JpaRepository<HopDong, Integer> {

    @Query("SELECT h FROM HopDong h JOIN FETCH h.phongTro "
            + "WHERE h.khachThue.id = :khachId ORDER BY h.ngayBatDau DESC")
    List<HopDong> findAllByKhach(@Param("khachId") Integer khachId);

    @Query("SELECT h FROM HopDong h JOIN FETCH h.phongTro "
            + "WHERE h.khachThue.id = :khachId AND h.trangThai = 'HIEU_LUC' ORDER BY h.ngayBatDau DESC")
    List<HopDong> findActiveByKhach(@Param("khachId") Integer khachId);

    @Query("SELECT h FROM HopDong h JOIN FETCH h.phongTro "
            + "WHERE h.khachThue.id = :khachId AND h.phongTro.id = :phongId AND h.trangThai = 'HIEU_LUC'")
    List<HopDong> findActiveByKhachAndPhong(@Param("khachId") Integer khachId,
                                            @Param("phongId") Integer phongId);
}
