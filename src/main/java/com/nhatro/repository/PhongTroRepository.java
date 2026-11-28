package com.nhatro.repository;

import com.nhatro.entity.PhongTro;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhongTroRepository extends JpaRepository<PhongTro, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhongTro p WHERE p.id = :id")
    Optional<PhongTro> findByIdForUpdate(@Param("id") Integer id);

    @Query("SELECT p.id FROM PhongTro p WHERE p.maPhong = :maPhong")
    Optional<Integer> findIdByMaPhong(@Param("maPhong") String maPhong);

    Optional<PhongTro> findByMaPhong(String maPhong);

    boolean existsByMaPhong(String maPhong);

    long countByTrangThai(String trangThai);

    List<PhongTro> findByTrangThaiOrderByMaPhongAsc(String trangThai);

    List<PhongTro> findByTrangThaiInOrderByMaPhongAsc(List<String> trangThais);

    @Query("SELECT p FROM PhongTro p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.maPhong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.tenPhong) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR p.trangThai = :trangThai)")
    Page<PhongTro> search(@Param("keyword") String keyword, @Param("trangThai") String trangThai, Pageable pageable);
}
