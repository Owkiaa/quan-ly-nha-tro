package com.nhatro.controller;

import com.nhatro.config.AuthInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root(HttpSession session) {
        if (session != null && session.getAttribute(AuthInterceptor.SESSION_USER) != null) {
            String role = (String) session.getAttribute(AuthInterceptor.SESSION_ROLE);
            if ("TENANT".equalsIgnoreCase(role)) {
                return "redirect:/tenant/rooms";
            }
            return "redirect:/phong-tro";
        }
        return "redirect:/login";
    }

    @GetMapping("/landing")
    public String landing(Model model) {
        model.addAttribute("title", "Hệ thống quản lí nhà trọ");
        model.addAttribute("status", "Khung đồ án Spring Boot đã sẵn sàng!");
        return "index";
    }
}
