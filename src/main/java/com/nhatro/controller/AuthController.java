package com.nhatro.controller;

import com.nhatro.config.AuthInterceptor;
import com.nhatro.entity.TaiKhoan;
import com.nhatro.exception.BusinessException;
import com.nhatro.service.TaiKhoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final TaiKhoanService taiKhoanService;

    public AuthController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "redirect", required = false) String redirect,
                                @RequestParam(value = "logout", required = false) String logout,
                                HttpSession session,
                                Model model) {
        // Nếu đã đăng nhập, chuyển hướng luôn vào trang chính
        if (session != null && session.getAttribute(AuthInterceptor.SESSION_USER) != null) {
            return "redirect:/phong-tro";
        }

        if (logout != null) {
            model.addAttribute("infoMessage", "Bạn đã đăng xuất khỏi hệ thống thành công");
        }
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }

        return "auth/login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("tenDangNhap") String tenDangNhap,
                              @RequestParam("matKhau") String matKhau,
                              @RequestParam(value = "redirect", required = false) String redirect,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            TaiKhoan taiKhoan = taiKhoanService.login(tenDangNhap, matKhau);

            session.setAttribute(AuthInterceptor.SESSION_USER, taiKhoan);
            session.setAttribute(AuthInterceptor.SESSION_USER_ID, taiKhoan.getId());
            session.setAttribute(AuthInterceptor.SESSION_USERNAME, taiKhoan.getTenDangNhap());
            session.setAttribute(AuthInterceptor.SESSION_ROLE, taiKhoan.getVaiTro());

            String fullName = taiKhoan.getTenDangNhap();
            if (taiKhoan.getKhachThue() != null && taiKhoan.getKhachThue().getHoTen() != null) {
                fullName = taiKhoan.getKhachThue().getHoTen();
            }
            session.setAttribute(AuthInterceptor.SESSION_FULLNAME, fullName);

            redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công! Chào mừng " + fullName);

            if (redirect != null && !redirect.trim().isEmpty() && !redirect.startsWith("/login")) {
                return "redirect:" + redirect;
            }

            return "redirect:/phong-tro";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("oldUsername", tenDangNhap);
            if (redirect != null && !redirect.trim().isEmpty()) {
                redirectAttributes.addAttribute("redirect", redirect);
            }
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout=true";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        TaiKhoan user = (session != null) ? (TaiKhoan) session.getAttribute(AuthInterceptor.SESSION_USER) : null;
        if (user == null) {
            return "redirect:/login";
        }
        TaiKhoan freshUser = taiKhoanService.getById(user.getId());
        model.addAttribute("user", freshUser);
        return "auth/profile";
    }

    @PostMapping("/profile/change-password")
    public String handleChangePassword(@RequestParam("oldPassword") String oldPassword,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        TaiKhoan user = (session != null) ? (TaiKhoan) session.getAttribute(AuthInterceptor.SESSION_USER) : null;
        if (user == null) {
            return "redirect:/login";
        }

        try {
            taiKhoanService.changePassword(user.getId(), oldPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/profile";
    }
}
