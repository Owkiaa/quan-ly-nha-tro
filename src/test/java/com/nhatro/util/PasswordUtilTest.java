package com.nhatro.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    @DisplayName("Băm mật khẩu mới phải sinh ra chuỗi có định dạng salt:hash và verify thành công")
    void testHashAndVerifySuccess() {
        String rawPassword = "MySecretPassword@2026";
        String hashed = PasswordUtil.hash(rawPassword);

        assertNotNull(hashed);
        assertTrue(hashed.contains(":"), "Mật khẩu băm phải chứa dấu hai chấm ngăn cách salt và hash");

        String[] parts = hashed.split(":");
        assertEquals(2, parts.length);
        assertEquals(16, parts[0].length(), "Muối ngẫu nhiên phải có 16 ký tự hex (8 bytes)");
        assertEquals(64, parts[1].length(), "Chuỗi băm SHA-256 phải có 64 ký tự hex");

        assertTrue(PasswordUtil.verify(rawPassword, hashed), "Xác thực mật khẩu đúng phải trả về true");
    }

    @Test
    @DisplayName("Xác thực với mật khẩu sai phải trả về false")
    void testVerifyWithWrongPassword() {
        String hashed = PasswordUtil.hash("CorrectPassword");
        assertFalse(PasswordUtil.verify("WrongPassword", hashed), "Xác thực với mật khẩu sai phải trả về false");
    }

    @Test
    @DisplayName("Xác thực chính xác với 3 bộ tài khoản mẫu trong CSDL NhaTroDB")
    void testSampleCredentialsFromDatabase() {
        // 1. Admin
        String adminHash = "a1b2c3d4e5f60718:9625a6c207ba950ac83bbd856c466408c021a28e296a970ece60f1fe2244742e";
        assertTrue(PasswordUtil.verify("Admin@123", adminHash), "Mật khẩu Admin@123 phải khớp với dữ liệu mẫu CSDL");
        assertFalse(PasswordUtil.verify("Admin@999", adminHash));

        // 2. Staff1
        String staffHash = "f1e2d3c4b5a60719:fa9c403b3a3c50e8e5bbfed4c8615e41f51dda576eb220293473fa8490dc5a54";
        assertTrue(PasswordUtil.verify("Staff@123", staffHash), "Mật khẩu Staff@123 phải khớp với dữ liệu mẫu CSDL");

        // 3. Tenant01
        String tenantHash = "8a7b6c5d4e3f2021:ba665a8f6c7d6b511be93aea8e08f6eac274da214a01bdc1a5e3b1d18489c924";
        assertTrue(PasswordUtil.verify("Tenant@123", tenantHash), "Mật khẩu Tenant@123 phải khớp với dữ liệu mẫu CSDL");
    }

    @Test
    @DisplayName("Hai lần băm cùng một mật khẩu phải sinh ra 2 salt khác nhau (chống Rainbow Table)")
    void testSaltIsUnique() {
        String p = "SamePassword123";
        String h1 = PasswordUtil.hash(p);
        String h2 = PasswordUtil.hash(p);

        assertNotEquals(h1, h2, "Mỗi lần băm phải sinh ra chuỗi muối ngẫu nhiên khác nhau");
        assertTrue(PasswordUtil.verify(p, h1));
        assertTrue(PasswordUtil.verify(p, h2));
    }
}
