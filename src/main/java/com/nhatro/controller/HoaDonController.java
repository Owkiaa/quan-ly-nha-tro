package com.nhatro.controller;

import com.nhatro.dto.ChiSoDienNuocDTO;
import com.nhatro.dto.HoaDonBatchFormDTO;
import com.nhatro.dto.HoaDonBatchItemDTO;
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
import java.util.Set;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonController {

    private static final Pattern THANG_NAM_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{4}$");
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "maHoaDon", "thangNam", "tongTien", "trangThai", "ngayLap");

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

        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            sortField = "id";
            direction = Sort.Direction.DESC;
        }
        String normalizedSort = sortField + "," + (direction == Sort.Direction.ASC ? "asc" : "desc");

        Pageable pageable = PageRequest.of(Math.max(0, page), size <= 0 ? 10 : size, Sort.by(direction, sortField));
        Page<HoaDon> pageData = hoaDonService.searchHoaDon(keyword, thangNam, trangThai, pageable);

        long countDaThanhToan = hoaDonService.countByTrangThai("DA_THANH_TOAN");
        long countChuaThanhToan = hoaDonService.countByTrangThai("CHUA_THANH_TOAN");
        long totalInvoices = countDaThanhToan + countChuaThanhToan;
        BigDecimal tongDaThu = hoaDonService.sumTongDoanhThuDaThu();
        BigDecimal tongNoChuaThu = hoaDonService.sumTongNoChuaThu();

        model.addAttribute("pageData", pageData);
        model.addAttribute("countDaThanhToan", countDaThanhToan);
        model.addAttribute("countChuaThanhToan", countChuaThanhToan);
        model.addAttribute("totalInvoices", totalInvoices);
        model.addAttribute("tongDaThu", tongDaThu);
        model.addAttribute("tongNoChuaThu", tongNoChuaThu);
        model.addAttribute("keyword", keyword);
        model.addAttribute("thangNam", thangNam);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sort", normalizedSort);
        model.addAttribute("currentPage", Math.max(0, page));
        model.addAttribute("totalPages", pageData.getTotalPages());

        return "hoa-don/danh-sach";
    }

    @GetMapping("/tao-moi")
    public String formTaoMoi(
            @RequestParam(name = "hopDongId", required = false) Integer hopDongId,
            Model model
    ) {
        List<HopDong> danhSachHopDong = hoaDonService.getDanhSachHopDongHieuLuc();
        HoaDonCreateDTO dto = new HoaDonCreateDTO();
        dto.setThangNam(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy")));

        if (hopDongId != null) {
            try {
                ChiSoDienNuocDTO prefill = hoaDonService.getChiSoDienNuoc(hopDongId);
                dto.setHopDongId(prefill.getHopDongId());
                if (prefill.getGiaThue() != null) {
                    dto.setTienPhong(prefill.getGiaThue());
                }
                dto.setChiSoDienCu(prefill.getChiSoDienCu());
                dto.setChiSoDienMoi(prefill.getChiSoDienCu());
                dto.setChiSoNuocCu(prefill.getChiSoNuocCu());
                dto.setChiSoNuocMoi(prefill.getChiSoNuocCu());
            } catch (BusinessException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }

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

    @GetMapping("/tao-hang-loat")
    public String formTaoHangLoat(
            @RequestParam(name = "thangNam", required = false) String thangNam,
            Model model
    ) {
        if (thangNam != null && !thangNam.isBlank() && !THANG_NAM_PATTERN.matcher(thangNam.trim()).matches()) {
            model.addAttribute("errorMessage", "Tháng/Năm phải đúng định dạng MM/yyyy (ví dụ: 11/2026).");
        }
        HoaDonBatchFormDTO batchForm = hoaDonService.prepareBatchForm(thangNam);
        model.addAttribute("batchForm", batchForm);
        return "hoa-don/tao-hang-loat";
    }

    @PostMapping("/tao-hang-loat")
    public String xuLyTaoHangLoat(
            @ModelAttribute("batchForm") HoaDonBatchFormDTO batchForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        int selectedCount = validateHoaDonBatchForm(batchForm, bindingResult);
        if (selectedCount == 0 && !bindingResult.hasFieldErrors("thangNam")) {
            hoaDonService.refreshBatchItemMetadata(batchForm);
            model.addAttribute("errorMessage", "Vui lòng chọn ít nhất một phòng để lập hóa đơn hàng loạt.");
            return "hoa-don/tao-hang-loat";
        }
        if (bindingResult.hasErrors()) {
            hoaDonService.refreshBatchItemMetadata(batchForm);
            model.addAttribute("errorMessage", "Dữ liệu nhập chưa hợp lệ, vui lòng kiểm tra lại các ô được đánh dấu đỏ.");
            return "hoa-don/tao-hang-loat";
        }

        try {
            List<HoaDon> createdList = hoaDonService.taoHoaDonHangLoat(batchForm);
            BigDecimal tongGiaTri = BigDecimal.ZERO;
            for (HoaDon hd : createdList) {
                tongGiaTri = tongGiaTri.add(hd.getTongTien());
            }
            String thangNamChuan = batchForm.getThangNam().trim();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Lập hàng loạt thành công " + createdList.size() + " hóa đơn cho kỳ " + thangNamChuan +
                            ". Tổng giá trị: " + String.format("%,.0f", tongGiaTri) + " VNĐ");
            redirectAttributes.addAttribute("thangNam", thangNamChuan);
            return "redirect:/hoa-don";
        } catch (BusinessException ex) {
            hoaDonService.refreshBatchItemMetadata(batchForm);
            model.addAttribute("errorMessage", ex.getMessage());
            return "hoa-don/tao-hang-loat";
        } catch (Exception ex) {
            hoaDonService.refreshBatchItemMetadata(batchForm);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn: " + ex.getMessage());
            return "hoa-don/tao-hang-loat";
        }
    }

    private void validateHoaDonCreateDTO(HoaDonCreateDTO dto, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("hopDongId") && dto.getHopDongId() == null) {
            bindingResult.rejectValue("hopDongId", "required", "Vui lòng chọn hợp đồng thuê phòng.");
        }

        validateThangNamField(dto.getThangNam(), bindingResult);
        validateDonGiaFields(dto.getDonGiaDien(), dto.getDonGiaNuoc(), bindingResult);
        validateMeterAndFeeFields("", dto.getChiSoDienCu(), dto.getChiSoDienMoi(),
                dto.getChiSoNuocCu(), dto.getChiSoNuocMoi(), dto.getTienPhong(), dto.getTienDichVu(), bindingResult);
    }

    private int validateHoaDonBatchForm(HoaDonBatchFormDTO form, BindingResult bindingResult) {
        validateThangNamField(form.getThangNam(), bindingResult);
        validateDonGiaFields(form.getDonGiaDien(), form.getDonGiaNuoc(), bindingResult);

        int selectedCount = 0;
        if (form.getItems() != null) {
            for (int i = 0; i < form.getItems().size(); i++) {
                HoaDonBatchItemDTO item = form.getItems().get(i);
                if (item.isSelected() && item.getHopDongId() != null) {
                    selectedCount++;
                    validateMeterAndFeeFields("items[" + i + "].", item.getChiSoDienCu(), item.getChiSoDienMoi(),
                            item.getChiSoNuocCu(), item.getChiSoNuocMoi(), item.getTienPhong(), item.getTienDichVu(), bindingResult);
                }
            }
        }
        return selectedCount;
    }

    private void validateThangNamField(String thangNam, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("thangNam")) {
            if (thangNam == null || thangNam.trim().isEmpty()) {
                bindingResult.rejectValue("thangNam", "required", "Tháng/Năm lập hóa đơn không được để trống.");
            } else if (!THANG_NAM_PATTERN.matcher(thangNam.trim()).matches()) {
                bindingResult.rejectValue("thangNam", "invalid", "Tháng/Năm phải đúng định dạng MM/yyyy (ví dụ: 10/2026).");
            }
        }
    }

    private void validateDonGiaFields(BigDecimal donGiaDien, BigDecimal donGiaNuoc, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("donGiaDien")) {
            if (donGiaDien == null || donGiaDien.compareTo(BigDecimal.ZERO) < 0) {
                bindingResult.rejectValue("donGiaDien", "min", "Đơn giá điện phải lớn hơn hoặc bằng 0.");
            }
        }
        if (!bindingResult.hasFieldErrors("donGiaNuoc")) {
            if (donGiaNuoc == null || donGiaNuoc.compareTo(BigDecimal.ZERO) < 0) {
                bindingResult.rejectValue("donGiaNuoc", "min", "Đơn giá nước phải lớn hơn hoặc bằng 0.");
            }
        }
    }

    private void validateMeterAndFeeFields(
            String prefix,
            Integer chiSoDienCu,
            Integer chiSoDienMoi,
            Integer chiSoNuocCu,
            Integer chiSoNuocMoi,
            BigDecimal tienPhong,
            BigDecimal tienDichVu,
            BindingResult bindingResult
    ) {
        String fieldDienCu = prefix + "chiSoDienCu";
        String fieldDienMoi = prefix + "chiSoDienMoi";
        String fieldNuocCu = prefix + "chiSoNuocCu";
        String fieldNuocMoi = prefix + "chiSoNuocMoi";
        String fieldTienPhong = prefix + "tienPhong";
        String fieldTienDichVu = prefix + "tienDichVu";

        if (!bindingResult.hasFieldErrors(fieldDienCu)) {
            if (chiSoDienCu == null || chiSoDienCu < 0) {
                bindingResult.rejectValue(fieldDienCu, "min", "Chỉ số điện cũ phải từ 0 trở lên.");
            }
        }

        if (!bindingResult.hasFieldErrors(fieldDienMoi)) {
            if (chiSoDienMoi == null || chiSoDienMoi < 0) {
                bindingResult.rejectValue(fieldDienMoi, "min", "Chỉ số điện mới phải từ 0 trở lên.");
            } else if (!bindingResult.hasFieldErrors(fieldDienCu)
                    && chiSoDienCu != null
                    && chiSoDienMoi < chiSoDienCu) {
                bindingResult.rejectValue(fieldDienMoi, "invalid", "Chỉ số điện mới phải lớn hơn hoặc bằng chỉ số điện cũ.");
            }
        }

        if (!bindingResult.hasFieldErrors(fieldNuocCu)) {
            if (chiSoNuocCu == null || chiSoNuocCu < 0) {
                bindingResult.rejectValue(fieldNuocCu, "min", "Chỉ số nước cũ phải từ 0 trở lên.");
            }
        }

        if (!bindingResult.hasFieldErrors(fieldNuocMoi)) {
            if (chiSoNuocMoi == null || chiSoNuocMoi < 0) {
                bindingResult.rejectValue(fieldNuocMoi, "min", "Chỉ số nước mới phải từ 0 trở lên.");
            } else if (!bindingResult.hasFieldErrors(fieldNuocCu)
                    && chiSoNuocCu != null
                    && chiSoNuocMoi < chiSoNuocCu) {
                bindingResult.rejectValue(fieldNuocMoi, "invalid", "Chỉ số nước mới phải lớn hơn hoặc bằng chỉ số nước cũ.");
            }
        }

        if (!bindingResult.hasFieldErrors(fieldTienPhong)) {
            if (tienPhong == null || tienPhong.compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue(fieldTienPhong, "min", "Tiền thuê phòng phải lớn hơn 0.");
            }
        }

        if (!bindingResult.hasFieldErrors(fieldTienDichVu)) {
            if (tienDichVu == null || tienDichVu.compareTo(BigDecimal.ZERO) < 0) {
                bindingResult.rejectValue(fieldTienDichVu, "min", "Phí dịch vụ phải lớn hơn hoặc bằng 0.");
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
        return "redirect:" + sanitizeRedirectUrl(redirectUrl);
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
        return "redirect:" + sanitizeRedirectUrl(redirectUrl);
    }

    @PostMapping("/{id}/xoa")
    public String xoaHoaDon(
            @PathVariable("id") Integer id,
            @RequestParam(name = "redirectUrl", defaultValue = "/hoa-don") String redirectUrl,
            RedirectAttributes redirectAttributes
    ) {
        try {
            hoaDonService.xoaHoaDon(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa hóa đơn nháp thành công.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:" + sanitizeRedirectUrl(redirectUrl);
    }

    private String sanitizeRedirectUrl(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return "/hoa-don";
        }
        String trimmed = redirectUrl.trim();
        if (!trimmed.startsWith("/") || trimmed.startsWith("//")) {
            return "/hoa-don";
        }
        return trimmed;
    }
}
