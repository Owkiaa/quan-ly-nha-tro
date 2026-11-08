package com.nhatro.controller;

import com.nhatro.entity.YeuCauSuaChua;
import com.nhatro.service.YeuCauSuaChuaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/yeu-cau-sua-chua")
public class YeuCauSuaChuaController {

    @Autowired
    private YeuCauSuaChuaService yeuCauSuaChuaService;

    // Hiển thị danh sách yêu cầu sửa chữa

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        // Sửa: Bỏ "- 1", dùng trực tiếp biến page từ giao diện gửi lên
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        Page<YeuCauSuaChua> yeuCauPage = yeuCauSuaChuaService.search(keyword, trangThai, pageable);

        model.addAttribute("yeuCauPage", yeuCauPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", yeuCauPage.getTotalPages());

        return "yeucausuachua/index";
    }

    // Xử lý phân công thợ
    @PostMapping("/phan-cong/{id}")
    public String phanCongTho(@PathVariable Integer id,
                              @RequestParam String thoSuaChua,
                              RedirectAttributes redirectAttributes) {
        try {
            yeuCauSuaChuaService.phanCongTho(id, thoSuaChua);
            redirectAttributes.addFlashAttribute("successMessage", "Đã phân công thợ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi phân công: " + e.getMessage());
        }
        return "redirect:/yeu-cau-sua-chua";
    }

    // Xử lý cập nhật chi phí và hoàn thành
    @PostMapping("/hoan-thanh/{id}")
    public String hoanThanh(@PathVariable Integer id,
                            @RequestParam BigDecimal chiPhi,
                            RedirectAttributes redirectAttributes) {
        try {
            yeuCauSuaChuaService.capNhatChiPhiVaHoanThanh(id, chiPhi);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật chi phí và đánh dấu hoàn thành!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/yeu-cau-sua-chua";
    }
}