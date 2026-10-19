package com.nhatro.repository;

import com.nhatro.entity.KhachThue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachThueRepository extends JpaRepository<KhachThue, Integer> {

    Optional<KhachThue> findByCccd(String cccd);

    boolean existsByCccd(String cccd);

    @Query("SELECT k FROM KhachThue k WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR k.cccd LIKE CONCAT('%', :keyword, '%') OR k.soDienThoai LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR k.trangThai = :trangThai)")
    Page<KhachThue> search(@Param("keyword") String keyword, @Param("trangThai") String trangThai, Pageable pageable);
}
