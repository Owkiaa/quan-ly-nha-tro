package com.nhatro.controller;

import com.nhatro.dto.ChiSoDienNuocDTO;
import com.nhatro.dto.HoaDonCreateDTO;
import com.nhatro.entity.HoaDon;
import com.nhatro.entity.HopDong;
import com.nhatro.exception.BusinessException;
import com.nhatro.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonController {

    private static final Pattern THANG_NAM_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{4}$");

    @Autowired
    private HoaDonService hoaDonService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping
    public String danhSach(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "thangNam", required = false) String thangNam,
            @RequestParam(name = "trangThai", required = false) String trangThai,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "id,desc") String sort,
            Model model
    ) {
        String[] sortParams = (sort != null && !sort.isBlank()) ? sort.split(",") : new String[]{"id", "desc"};
        String sortField = sortParams[0].trim();
        Sort.Direction direction = (sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<HoaDon> pageData = hoaDonService.searchHoaDon(keyword, thangNam, trangThai, pageable);

        long countDaThanhToan = hoaDonService.countByTrangThai("DA_THANH_TOAN");
        long countChuaThanhToan = hoaDonService.countByTrangThai("CHUA_THANH_TOAN");
        BigDecimal tongDaThu = hoaDonService.sumTongDoanhThuDaThu();

        model.addAttribute("pageData", pageData);
        model.addAttribute("countDaThanhToan", countDaThanhToan);
        model.addAttribute("countChuaThanhToan", countChuaThanhToan);
        model.addAttribute("tongDaThu", tongDaThu);
        model.addAttribute("keyword", keyword);
        model.addAttribute("thangNam", thangNam);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());

        return "hoa-don/danh-sach";
    }

    @GetMapping("/tao-moi")
    public String formTaoMoi(Model model) {
        List<HopDong> danhSachHopDong = hoaDonService.getDanhSachHopDongHieuLuc();
        HoaDonCreateDTO dto = new HoaDonCreateDTO();
        dto.setThangNam(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy")));

        model.addAttribute("hoaDonDTO", dto);
        model.addAttribute("danhSachHopDong", danhSachHopDong);
        return "hoa-don/tao-moi";
    }

    @GetMapping("/api/chi-so-cu/{hopDongId}")
    @ResponseBody
    public ResponseEntity<ChiSoDienNuocDTO> layChiSoDienNuoc(@PathVariable("hopDongId") Integer hopDongId) {
        try {
            ChiSoDienNuocDTO dto = hoaDonService.getChiSoDienNuoc(hopDongId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/tao-moi")
    public String xuLyTaoMoi(
            @ModelAttribute("hoaDonDTO") HoaDonCreateDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        validateHoaDonCreateDTO(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachHopDong", hoaDonService.getDanhSachHopDongHieuLuc());
            return "hoa-don/tao-moi";
        }

        try {
            HoaDon hoaDonMoi = hoaDonService.taoHoaDon(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Lập thành công hóa đơn [" + hoaDonMoi.getMaHoaDon() + "] cho phòng " +
                            (hoaDonMoi.getHopDong().getPhongTro() != null ? hoaDonMoi.getHopDong().getPhongTro().getMaPhong() : "") +
                            ". Tổng tiền: " + String.format("%,.0f", hoaDonMoi.getTongTien()) + " VNĐ");
            return "redirect:/hoa-don";
        } catch (BusinessException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("đã có hóa đơn trong tháng")) {
                bindingResult.rejectValue("thangNam", "duplicate", ex.getMessage());
            } else if (msg.contains("chỉ số điện")) {
                bindingResult.rejectValue("chiSoDienMoi", "invalid", ex.getMessage());
            } else if (msg.contains("chỉ số nước")) {
                bindingResult.rejectValue("chiSoNuocMoi", "invalid", ex.getMessage());
            }
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("danhSachHopDong", hoaDonService.getDanhSachHopDongHieuLuc());
            return "hoa-don/tao-moi";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn: " + ex.getMessage());
            model.addAttribute("danhSachHopDong", hoaDonService.getDanhSachHopDongHieuLuc());
            return "hoa-don/tao-moi";
        }
    }

    private void validateHoaDonCreateDTO(HoaDonCreateDTO dto, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("hopDongId") && dto.getHopDongId() == null) {
            bindingResult.rejectValue("hopDongId", "required", "Vui lòng chọn hợp đồng thuê phòng.");
        }

        if (!bindingResult.hasFieldErrors("thangNam")) {
            if (dto.getThangNam() == null || dto.getThangNam().trim().isEmpty()) {
                bindingResult.rejectValue("thangNam", "required", "Tháng/Năm lập hóa đơn không được để trống.");
            } else if (!THANG_NAM_PATTERN.matcher(dto.getThangNam().trim()).matches()) {
                bindingResult.rejectValue("thangNam", "invalid", "Tháng/Năm phải đúng định dạng MM/yyyy (ví dụ: 10/2026).");
            }
        }

        if (!bindingResult.hasFieldErrors("chiSoDienCu")) {
            if (dto.getChiSoDienCu() == null || dto.getChiSoDienCu() < 0) {
                bindingResult.rejectValue("chiSoDienCu", "min", "Chỉ số điện cũ phải từ 0 trở lên.");
            }
        }

        if (!bindingResult.hasFieldErrors("chiSoDienMoi")) {
            if (dto.getChiSoDienMoi() == null || dto.getChiSoDienMoi() < 0) {
                bindingResult.rejectValue("chiSoDienMoi", "min", "Chỉ số điện mới phải từ 0 trở lên.");
            } else if (!bindingResult.hasFieldErrors("chiSoDienCu")
                    && dto.getChiSoDienCu() != null
                    && dto.getChiSoDienMoi() < dto.getChiSoDienCu()) {
                bindingResult.rejectValue("chiSoDienMoi", "invalid", "Chỉ số điện mới phải lớn hơn hoặc bằng chỉ số điện cũ.");
            }
        }

        if (!bindingResult.hasFieldErrors("donGiaDien")) {
            if (dto.getDonGiaDien() == null || dto.getDonGiaDien().compareTo(BigDecimal.ZERO) < 0) {
                bindingResult.rejectValue("donGiaDien", "min", "Đơn giá điện phải lớn hơn hoặc bằng 0.");
            }
        }

        if (!bindingResult.hasFieldErrors("chiSoNuocCu")) {
            if (dto.getChiSoNuocCu() == null || dto.getChiSoNuocCu() < 0) {
                bindingResult.rejectValue("chiSoNuocCu", "min", "Chỉ số nước cũ phải từ 0 trở lên.");
            }
        }

        if (!bindingResult.hasFieldErrors("chiSoNuocMoi")) {
            if (dto.getChiSoNuocMoi() == null || dto.getChiSoNuocMoi() < 0) {
                bindingResult.rejectValue("chiSoNuocMoi", "min", "Chỉ số nước mới phải từ 0 trở lên.");
            } else if (!bindingResult.hasFieldErrors("chiSoNuocCu")
                    && dto.getChiSoNuocCu() != null
                    && dto.getChiSoNuocMoi() < dto.getChiSoNuocCu()) {
                bindingResult.rejectValue("chiSoNuocMoi", "invalid", "Chỉ số nước mới phải lớn hơn hoặc bằng chỉ số nước cũ.");
            }
        }

        if (!bindingResult.hasFieldErrors("donGiaNuoc")) {
            if (dto.getDonGiaNuoc() == null || dto.getDonGiaNuoc().compareTo(BigDecimal.ZERO) < 0) {
                bindingResult.rejectValue("donGiaNuoc", "min", "Đơn giá nước phải lớn hơn hoặc bằng 0.");
            }
        }

        if (!bindingResult.hasFieldErrors("tienPhong")) {
            if (dto.getTienPhong() == null || dto.getTienPhong().compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("tienPhong", "min", "Tiền thuê phòng phải lớn hơn 0.");
            }
        }

        if (!bindingResult.hasFieldErrors("tienDichVu")) {
            if (dto.getTienDichVu() == null || dto.getTienDichVu().compareTo(BigDecimal.ZERO) < 0) {
                bindingResult.rejectValue("tienDichVu", "min", "Phí dịch vụ phải lớn hơn hoặc bằng 0.");
            }
        }
    }

    @GetMapping("/{id}")
    public String xemChiTiet(@PathVariable("id") Integer id, Model model) {
        HoaDon hoaDon = hoaDonService.getById(id);
        model.addAttribute("hoaDon", hoaDon);
        return "hoa-don/chi-tiet";
    }

    @GetMapping("/{id}/in")
    public String inHoaDon(@PathVariable("id") Integer id, Model model) {
        HoaDon hoaDon = hoaDonService.getById(id);
        model.addAttribute("hoaDon", hoaDon);
        return "hoa-don/in-hoa-don";
    }

    @PostMapping("/{id}/thanh-toan")
    public String xacNhanThanhToan(
            @PathVariable("id") Integer id,
            @RequestParam(name = "redirectUrl", defaultValue = "/hoa-don") String redirectUrl,
            RedirectAttributes redirectAttributes
    ) {
        try {
            hoaDonService.xacNhanThanhToan(id);
            redirectAttributes.addFlashAttribute("successMessage", "Gạch nợ thành công! Hóa đơn đã được chuyển sang trạng thái ĐÃ THANH TOÁN.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/{id}/huy-thanh-toan")
    public String huyThanhToan(
            @PathVariable("id") Integer id,
            @RequestParam(name = "redirectUrl", defaultValue = "/hoa-don") String redirectUrl,
            RedirectAttributes redirectAttributes
    ) {
        try {
            hoaDonService.huyThanhToan(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hoàn tác trạng thái hóa đơn về CHƯA THANH TOÁN.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:" + redirectUrl;
    }
}
