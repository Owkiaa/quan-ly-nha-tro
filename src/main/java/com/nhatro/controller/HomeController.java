package com.nhatro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Hệ thống quản lí nhà trọ");
        model.addAttribute("status", "Khung đồ án Spring Boot đã sẵn sàng!");
        return "index";
    }
}
