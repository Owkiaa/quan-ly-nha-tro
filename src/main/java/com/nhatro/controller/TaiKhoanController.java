package com.nhatro.controller;

import com.nhatro.config.AuthInterceptor;
import com.nhatro.entity.KhachThue;
import com.nhatro.entity.TaiKhoan;
import com.nhatro.exception.BusinessException;
import com.nhatro.repository.KhachThueRepository;
import com.nhatro.service.TaiKhoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/tai-khoan")
public class TaiKhoanController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "tenDangNhap", "vaiTro", "trangThai", "ngayTao"
    );

    private final TaiKhoanService taiKhoanService;
    private final KhachThueRepository khachThueRepository;

    public TaiKhoanController(TaiKhoanService taiKhoanService, KhachThueRepository khachThueRepository) {
        this.taiKhoanService = taiKhoanService;
        this.khachThueRepository = khachThueRepository;
    }

    @GetMapping
    public String listTaiKhoan(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "vaiTro", required = false) String vaiTro,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               @RequestParam(value = "sort", defaultValue = "id,desc") String sort,
                               Model model) {
        String[] sortParams = sort.split(",");
        String sortField = ALLOWED_SORT_FIELDS.contains(sortParams[0]) ? sortParams[0] : "id";
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        if (!ALLOWED_SORT_FIELDS.contains(sortParams[0])) {
            direction = Sort.Direction.DESC;
            sort = "id,desc";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<TaiKhoan> pageData = taiKhoanService.search(keyword, vaiTro, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("vaiTro", vaiTro);
        model.addAttribute("sort", sort);
        return "tai-khoan/list";
    }

    @GetMapping("/them")
    public String showAddForm(Model model) {
        model.addAttribute("taiKhoan", new TaiKhoan());
        model.addAttribute("danhSachKhach", khachThueRepository.findAll());
        return "tai-khoan/form";
    }

    @PostMapping("/them")
    public String handleAdd(@ModelAttribute("taiKhoan") TaiKhoan taiKhoan,
                            @RequestParam("rawPassword") String rawPassword,
                            @RequestParam(value = "khachId", required = false) Integer khachId,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            if (khachId != null) {
                KhachThue khach = khachThueRepository.findById(khachId).orElse(null);
                taiKhoan.setKhachThue(khach);
            }
            taiKhoanService.create(taiKhoan, rawPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm tài khoản mới thành công!");
            return "redirect:/tai-khoan";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("danhSachKhach", khachThueRepository.findAll());
            model.addAttribute("selectedKhachId", khachId);
            return "tai-khoan/form";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable("id") Integer id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            Integer currentUserId = (Integer) session.getAttribute(AuthInterceptor.SESSION_USER_ID);
            taiKhoanService.toggleStatus(id, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái tài khoản thành công!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tai-khoan";
    }

    @PostMapping("/reset/{id}")
    public String resetPassword(@PathVariable("id") Integer id,
                                @RequestParam(value = "newPassword", defaultValue = "123456") String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu thành công về: " + newPassword);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể đặt lại mật khẩu: " + ex.getMessage());
        }
        return "redirect:/tai-khoan";
    }
}
