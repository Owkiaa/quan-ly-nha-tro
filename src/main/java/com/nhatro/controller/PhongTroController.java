package com.nhatro.controller;

import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.service.PhongTroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/phong-tro")
public class PhongTroController {

    private final PhongTroService phongTroService;

    public PhongTroController(PhongTroService phongTroService) {
        this.phongTroService = phongTroService;
    }

    @GetMapping
    public String listPhongTro(@RequestParam(value = "keyword", required = false) String keyword,
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
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            phongTroService.create(phongTro, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm phòng trọ mới " + phongTro.getMaPhong() + " thành công!");
            return "redirect:/phong-tro";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("phongTro", phongTro);
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
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            phongTroService.update(id, phongTro, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phòng trọ " + phongTro.getMaPhong() + " thành công!");
            return "redirect:/phong-tro";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("phongTro", phongTro);
            model.addAttribute("isEdit", true);
            return "phong-tro/form";
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
