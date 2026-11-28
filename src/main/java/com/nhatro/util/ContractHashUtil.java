package com.nhatro.util;

import com.nhatro.entity.HopDong;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích băm dữ liệu và kiểm tra tính toàn vẹn của hợp đồng thuê phòng (SHA-256).
 * Đảm bảo tính minh bạch, chống giả mạo hoặc sửa đổi trái phép dữ liệu hợp đồng trong CSDL.
 * 
 * Phụ trách bởi: Bùi Trần Tấn Tài
 */
public class ContractHashUtil {

    private static final String HASH_SALT = "NhaTroSystem_Contract_Security_Salt_2026";

    /**
     * Tạo mã băm SHA-256 cho hợp đồng từ các trường dữ liệu cốt lõi:
     * maHopDong | cccdKhachThue | maPhong | ngayBatDau | giaThue | tienDatCoc
     */
    public static String generateContractHash(HopDong hopDong) {
        if (hopDong == null) {
            return "";
        }

        String rawData = String.format("%s|%s|%s|%s|%s|%s|%s",
                hopDong.getMaHopDong() != null ? hopDong.getMaHopDong().trim() : "",
                hopDong.getKhachThue() != null && hopDong.getKhachThue().getCccd() != null ? hopDong.getKhachThue().getCccd().trim() : "",
                hopDong.getPhongTro() != null && hopDong.getPhongTro().getMaPhong() != null ? hopDong.getPhongTro().getMaPhong().trim() : "",
                hopDong.getNgayBatDau() != null ? hopDong.getNgayBatDau().toString() : "",
                hopDong.getGiaThueThoaThuan() != null ? hopDong.getGiaThueThoaThuan().stripTrailingZeros().toPlainString() : "0",
                hopDong.getTienDatCoc() != null ? hopDong.getTienDatCoc().stripTrailingZeros().toPlainString() : "0",
                HASH_SALT
        );

        return sha256Hex(rawData);
    }

    /**
     * Kiểm tra tính toàn vẹn của hợp đồng dựa trên mã băm kỳ vọng.
     */
    public static boolean verifyContractHash(HopDong hopDong, String expectedHash) {
        if (hopDong == null || expectedHash == null || expectedHash.isBlank()) {
            return false;
        }
        String currentHash = generateContractHash(hopDong);
        return currentHash.equalsIgnoreCase(expectedHash.trim());
    }

    /**
     * Thuật toán SHA-256 chuẩn Java (java.security.MessageDigest).
     */
    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Thuật toán SHA-256 không được hỗ trợ trên môi trường này.", e);
        }
    }

    /**
     * Tạo đường dẫn URL mã QR để người dùng tra cứu và đối soát hợp đồng từ xa.
     */
    public static String buildQrCodeUrl(String targetUrl) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return "";
        }
        return "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data="
                + URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);
    }
}
