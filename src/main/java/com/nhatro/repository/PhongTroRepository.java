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

import java.math.BigDecimal;
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

    // ---------------------------------------------------------------- Cổng Khách thuê (TV3)

    @Query("SELECT p FROM PhongTro p WHERE p.trangThai = 'TRONG' AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.maPhong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.tenPhong) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:loaiPhong IS NULL OR :loaiPhong = '' OR p.loaiPhong = :loaiPhong) AND " +
           "p.giaThue >= :giaTu AND p.giaThue <= :giaDen AND " +
           "COALESCE(p.dienTich, 0.0) >= :dienTichToiThieu")
    Page<PhongTro> searchAvailable(@Param("keyword") String keyword,
                                   @Param("loaiPhong") String loaiPhong,
                                   @Param("giaTu") BigDecimal giaTu,
                                   @Param("giaDen") BigDecimal giaDen,
                                   @Param("dienTichToiThieu") Double dienTichToiThieu,
                                   Pageable pageable);

    @Query("SELECT DISTINCT p.loaiPhong FROM PhongTro p WHERE p.loaiPhong IS NOT NULL ORDER BY p.loaiPhong")
    List<String> findDistinctLoaiPhong();
}
