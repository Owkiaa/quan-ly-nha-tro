package com.nhatro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TẠM THỜI: giả lập đăng nhập khách thuê để thử Cổng Khách thuê khi TV1 chưa xong đăng nhập.
 * Mở: http://localhost:8080/demo/tenant   (mặc định hồ sơ khách số 2; đổi bằng ?khachId=3)
 * XÓA file này khi đăng nhập thật đã chạy.
 */
@Controller
@RequestMapping("/demo")
public class DemoSessionController {

    @GetMapping("/tenant")
    public String loginAsTenant(@RequestParam(defaultValue = "2") Integer khachId, HttpSession session) {
        session.setAttribute("SESSION_ROLE", "TENANT");
        session.setAttribute("SESSION_FULLNAME", "Khách demo (hồ sơ " + khachId + ")");
        session.setAttribute("SESSION_KHACH_ID", khachId);
        return "redirect:/tenant/rooms";
    }
}
