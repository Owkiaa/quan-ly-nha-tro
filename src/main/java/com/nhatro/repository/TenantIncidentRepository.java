package com.nhatro.repository;

import com.nhatro.entity.YeuCauSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Truy vấn phiếu phản ánh sự cố THEO KHÁCH THUÊ cho Cổng Khách thuê (TV3). */
@Repository
public interface TenantIncidentRepository extends JpaRepository<YeuCauSuaChua, Integer> {

    @Query("SELECT y FROM YeuCauSuaChua y JOIN FETCH y.phongTro "
            + "WHERE y.khachThue.id = :khachId ORDER BY y.ngayTao DESC")
    List<YeuCauSuaChua> findAllByKhach(@Param("khachId") Integer khachId);
}
