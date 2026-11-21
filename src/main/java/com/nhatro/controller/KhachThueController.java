package com.nhatro.controller;

import com.nhatro.entity.KhachThue;
import com.nhatro.exception.BusinessException;
import com.nhatro.service.KhachThueService;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/khach-thue")
public class KhachThueController {

    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9,12}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final KhachThueService khachThueService;

    public KhachThueController(KhachThueService khachThueService) {
        this.khachThueService = khachThueService;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
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
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        validateKhachThue(khachThue, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "khach-thue/form";
        }
        try {
            khachThueService.create(khachThue);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm khách thuê " + khachThue.getHoTen() + " thành công!");
            return "redirect:/khach-thue";
        } catch (BusinessException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("cccd")) {
                bindingResult.rejectValue("cccd", "duplicate", ex.getMessage());
            }
            model.addAttribute("errorMessage", ex.getMessage());
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
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        validateKhachThue(khachThue, bindingResult);
        if (bindingResult.hasErrors()) {
            khachThue.setId(id);
            model.addAttribute("isEdit", true);
            return "khach-thue/form";
        }
        try {
            khachThueService.update(id, khachThue);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin khách thuê " + khachThue.getHoTen() + " thành công!");
            return "redirect:/khach-thue";
        } catch (BusinessException ex) {
            khachThue.setId(id);
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("cccd")) {
                bindingResult.rejectValue("cccd", "duplicate", ex.getMessage());
            }
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isEdit", true);
            return "khach-thue/form";
        }
    }

    private void validateKhachThue(KhachThue khachThue, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("hoTen")) {
            if (khachThue.getHoTen() == null || khachThue.getHoTen().trim().isEmpty()) {
                bindingResult.rejectValue("hoTen", "required", "Họ và tên khách thuê không được để trống.");
            } else if (khachThue.getHoTen().trim().length() > 100) {
                bindingResult.rejectValue("hoTen", "maxlength", "Họ và tên không được vượt quá 100 ký tự.");
            }
        }

        if (!bindingResult.hasFieldErrors("cccd")) {
            if (khachThue.getCccd() == null || khachThue.getCccd().trim().isEmpty()) {
                bindingResult.rejectValue("cccd", "required", "Số CCCD / CMND không được để trống.");
            } else if (!CCCD_PATTERN.matcher(khachThue.getCccd().trim()).matches()) {
                bindingResult.rejectValue("cccd", "invalid", "Số CCCD phải gồm từ 9 đến 12 chữ số.");
            }
        }

        if (!bindingResult.hasFieldErrors("soDienThoai")) {
            if (khachThue.getSoDienThoai() == null || khachThue.getSoDienThoai().trim().isEmpty()) {
                bindingResult.rejectValue("soDienThoai", "required", "Số điện thoại không được để trống.");
            } else if (!PHONE_PATTERN.matcher(khachThue.getSoDienThoai().trim()).matches()) {
                bindingResult.rejectValue("soDienThoai", "invalid", "Số điện thoại không hợp lệ (10–11 chữ số, bắt đầu bằng 0).");
            }
        }

        if (!bindingResult.hasFieldErrors("email") && khachThue.getEmail() != null && !khachThue.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(khachThue.getEmail().trim()).matches()) {
                bindingResult.rejectValue("email", "invalid", "Địa chỉ email không đúng định dạng.");
            }
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
