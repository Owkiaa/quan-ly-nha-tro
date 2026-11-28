package com.nhatro.controller;

import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.service.PhongTroService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Set;

@Controller
@RequestMapping("/phong-tro")
public class PhongTroController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "maPhong", "tenPhong", "loaiPhong", "dienTich", "giaThue", "trangThai"
    );

    private final PhongTroService phongTroService;

    public PhongTroController(PhongTroService phongTroService) {
        this.phongTroService = phongTroService;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping
    public String listPhongTro(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "trangThai", required = false) String trangThai,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "20") int size,
                               @RequestParam(value = "sort", defaultValue = "maPhong,asc") String sort,
                               Model model) {
        String[] sortParams = sort.split(",");
        String sortField = ALLOWED_SORT_FIELDS.contains(sortParams[0]) ? sortParams[0] : "maPhong";
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        if (!ALLOWED_SORT_FIELDS.contains(sortParams[0])) {
            direction = Sort.Direction.ASC;
            sort = "maPhong,asc";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<PhongTro> pageData = phongTroService.search(keyword, trangThai, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sort", sort);

        // Thống kê nhanh
        model.addAttribute("totalRooms", phongTroService.countAll());
        model.addAttribute("countTrong", phongTroService.countByTrangThai("TRONG"));
        model.addAttribute("countDaDat", phongTroService.countByTrangThai("DA_DAT"));
        model.addAttribute("countDaThue", phongTroService.countByTrangThai("DA_THUE"));
        model.addAttribute("countBaoTri", phongTroService.countByTrangThai("BAO_TRI"));

        return "phong-tro/list";
    }

    @GetMapping("/them")
    public String showAddForm(Model model) {
        PhongTro phongTro = new PhongTro();
        phongTro.setTrangThai("TRONG");
        phongTro.setLoaiPhong("Đơn");
        model.addAttribute("phongTro", phongTro);
        model.addAttribute("isEdit", false);
        return "phong-tro/form";
    }

    @PostMapping("/them")
    public String handleAdd(@ModelAttribute("phongTro") PhongTro phongTro,
                            BindingResult bindingResult,
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        validatePhongTro(phongTro, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "phong-tro/form";
        }
        try {
            phongTroService.create(phongTro, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm phòng trọ mới " + phongTro.getMaPhong() + " thành công!");
            return "redirect:/phong-tro";
        } catch (BusinessException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("mã phòng")) {
                bindingResult.rejectValue("maPhong", "duplicate", ex.getMessage());
            }
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isEdit", false);
            return "phong-tro/form";
        }
    }

    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        PhongTro phongTro = phongTroService.getById(id);
        model.addAttribute("phongTro", phongTro);
        model.addAttribute("isEdit", true);
        return "phong-tro/form";
    }

    @PostMapping("/sua/{id}")
    public String handleEdit(@PathVariable("id") Integer id,
                             @ModelAttribute("phongTro") PhongTro phongTro,
                             BindingResult bindingResult,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        validatePhongTro(phongTro, bindingResult);
        if (bindingResult.hasErrors()) {
            phongTro.setId(id);
            model.addAttribute("isEdit", true);
            return "phong-tro/form";
        }
        try {
            phongTroService.update(id, phongTro, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phòng trọ " + phongTro.getMaPhong() + " thành công!");
            return "redirect:/phong-tro";
        } catch (BusinessException ex) {
            phongTro.setId(id);
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("mã phòng")) {
                bindingResult.rejectValue("maPhong", "duplicate", ex.getMessage());
            }
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isEdit", true);
            return "phong-tro/form";
        }
    }

    private void validatePhongTro(PhongTro phongTro, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("maPhong")) {
            if (phongTro.getMaPhong() == null || phongTro.getMaPhong().trim().isEmpty()) {
                bindingResult.rejectValue("maPhong", "required", "Mã phòng không được để trống.");
            } else if (phongTro.getMaPhong().trim().length() > 20) {
                bindingResult.rejectValue("maPhong", "maxlength", "Mã phòng không được vượt quá 20 ký tự.");
            }
        }

        if (!bindingResult.hasFieldErrors("tenPhong")) {
            if (phongTro.getTenPhong() == null || phongTro.getTenPhong().trim().isEmpty()) {
                bindingResult.rejectValue("tenPhong", "required", "Tên phòng không được để trống.");
            } else if (phongTro.getTenPhong().trim().length() > 50) {
                bindingResult.rejectValue("tenPhong", "maxlength", "Tên phòng không được vượt quá 50 ký tự.");
            }
        }

        if (!bindingResult.hasFieldErrors("dienTich") && phongTro.getDienTich() != null && phongTro.getDienTich() <= 0) {
            bindingResult.rejectValue("dienTich", "min", "Diện tích phải lớn hơn 0 m².");
        }

        if (!bindingResult.hasFieldErrors("giaThue")) {
            if (phongTro.getGiaThue() == null || phongTro.getGiaThue().compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("giaThue", "min", "Giá thuê phải lớn hơn 0 VNĐ.");
            }
        }

        if (!bindingResult.hasFieldErrors("trangThai")) {
            if (phongTro.getTrangThai() == null || phongTro.getTrangThai().trim().isEmpty()) {
                bindingResult.rejectValue("trangThai", "required", "Vui lòng chọn trạng thái phòng.");
            }
        }
    }

    @PostMapping("/xoa/{id}")
    public String handleDelete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            phongTroService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phòng trọ thành công!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/phong-tro";
    }

    @GetMapping("/chi-tiet/{id}")
    public String showDetail(@PathVariable("id") Integer id, Model model) {
        PhongTro phongTro = phongTroService.getById(id);
        model.addAttribute("phongTro", phongTro);
        return "phong-tro/detail";
    }
}
