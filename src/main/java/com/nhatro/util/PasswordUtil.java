package com.nhatro.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Tiện ích mã hóa và xác thực mật khẩu bằng SHA-256 kèm muối (Salt).
 * Tuân thủ yêu cầu: Sử dụng thư viện chuẩn java.security.MessageDigest,
 * không dùng Spring Security hay BCrypt.
 * Định dạng lưu trữ: salt:hash (ví dụ: a1b2c3d4e5f60718:9625a6c2...)
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Tạo chuỗi muối (salt) ngẫu nhiên 8 bytes (16 ký tự hex)
     */
    public static String generateSalt() {
        byte[] saltBytes = new byte[8];
        RANDOM.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /**
     * Băm mật khẩu với chuỗi muối đã cho: SHA-256(salt + rawPassword)
     */
    public static String hashWithSalt(String rawPassword, String salt) {
        if (rawPassword == null || salt == null) {
            throw new IllegalArgumentException("Mật khẩu và muối không được để trống");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            String input = salt + rawPassword;
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Không tìm thấy thuật toán SHA-256", e);
        }
    }

    /**
     * Băm mật khẩu mới (tự sinh muối ngẫu nhiên), trả về dạng salt:hash
     */
    public static String hash(String rawPassword) {
        String salt = generateSalt();
        String hash = hashWithSalt(rawPassword, salt);
        return salt + ":" + hash;
    }

    /**
     * So khớp mật khẩu người dùng nhập vào với chuỗi mã hóa trong CSDL
     */
    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }

        if (storedHash.contains(":")) {
            String[] parts = storedHash.split(":", 2);
            String salt = parts[0];
            String expectedHash = parts[1];
            String actualHash = hashWithSalt(rawPassword, salt);
            return expectedHash.equalsIgnoreCase(actualHash);
        }

        // Trường hợp mật khẩu cũ không có muối (plain SHA-256)
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return storedHash.equalsIgnoreCase(bytesToHex(hashBytes));
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
