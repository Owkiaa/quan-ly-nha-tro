package com.nhatro.repository;

import com.nhatro.entity.TaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {

    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByKhachThueId(Integer khachId);

    @Query("SELECT t FROM TaiKhoan t LEFT JOIN FETCH t.khachThue WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(t.tenDangNhap) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:vaiTro IS NULL OR :vaiTro = '' OR t.vaiTro = :vaiTro)")
    Page<TaiKhoan> search(@Param("keyword") String keyword, @Param("vaiTro") String vaiTro, Pageable pageable);
}
