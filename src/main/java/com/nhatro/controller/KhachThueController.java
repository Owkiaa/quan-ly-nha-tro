package com.nhatro.controller;

import com.nhatro.entity.KhachThue;
import com.nhatro.exception.BusinessException;
import com.nhatro.service.KhachThueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/khach-thue")
public class KhachThueController {

    private final KhachThueService khachThueService;

    public KhachThueController(KhachThueService khachThueService) {
        this.khachThueService = khachThueService;
    }

    @GetMapping
    public String listKhachThue(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "trangThai", required = false) String trangThai,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                @RequestParam(value = "sort", defaultValue = "id,desc") String sort,
                                Model model) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<KhachThue> pageData = khachThueService.search(keyword, trangThai, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sort", sort);

        // Thống kê nhanh
        model.addAttribute("totalTenants", khachThueService.countAll());
        model.addAttribute("countDangThue", khachThueService.countByTrangThai("DANG_THUE"));
        model.addAttribute("countDaTraPhong", khachThueService.countByTrangThai("DA_TRA_PHONG"));

        return "khach-thue/list";
    }

    @GetMapping("/them")
    public String showAddForm(Model model) {
        KhachThue khachThue = new KhachThue();
        khachThue.setTrangThai("DANG_THUE");
        khachThue.setGioiTinh("Nam");
        model.addAttribute("khachThue", khachThue);
        model.addAttribute("isEdit", false);
        return "khach-thue/form";
    }

    @PostMapping("/them")
    public String handleAdd(@ModelAttribute("khachThue") KhachThue khachThue,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            khachThueService.create(khachThue);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm khách thuê " + khachThue.getHoTen() + " thành công!");
            return "redirect:/khach-thue";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("khachThue", khachThue);
            model.addAttribute("isEdit", false);
            return "khach-thue/form";
        }
    }

    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        KhachThue khachThue = khachThueService.getById(id);
        model.addAttribute("khachThue", khachThue);
        model.addAttribute("isEdit", true);
        return "khach-thue/form";
    }

    @PostMapping("/sua/{id}")
    public String handleEdit(@PathVariable("id") Integer id,
                             @ModelAttribute("khachThue") KhachThue khachThue,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            khachThueService.update(id, khachThue);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin khách thuê " + khachThue.getHoTen() + " thành công!");
            return "redirect:/khach-thue";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("khachThue", khachThue);
            model.addAttribute("isEdit", true);
            return "khach-thue/form";
        }
    }

    @PostMapping("/xoa/{id}")
    public String handleDelete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            khachThueService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa khách thuê thành công!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/khach-thue";
    }

    @GetMapping("/chi-tiet/{id}")
    public String showDetail(@PathVariable("id") Integer id, Model model) {
        KhachThue khachThue = khachThueService.getById(id);
        model.addAttribute("khachThue", khachThue);
        return "khach-thue/detail";
    }
}
