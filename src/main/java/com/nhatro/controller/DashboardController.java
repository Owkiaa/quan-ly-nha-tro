package com.nhatro.controller;

import com.nhatro.dto.DashboardDTO;
import com.nhatro.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/dashboard", "/thong-ke"})
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public String index(Model model) {
        DashboardDTO stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "dashboard/index";
    }
}
