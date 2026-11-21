package com.nhatro.controller;

import com.nhatro.dto.ChiSoDienNuocDTO;
import com.nhatro.dto.HoaDonCreateDTO;
import com.nhatro.entity.HoaDon;
import com.nhatro.entity.HopDong;
import com.nhatro.exception.BusinessException;
import com.nhatro.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping
    public String danhSach(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "thangNam", required = false) String thangNam,
            @RequestParam(name = "trangThai", required = false) String trangThai,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<HoaDon> pageData = hoaDonService.searchHoaDon(keyword, thangNam, trangThai, pageable);

        long countDaThanhToan = hoaDonService.countByTrangThai("DA_THANH_TOAN");
        long countChuaThanhToan = hoaDonService.countByTrangThai("CHUA_THANH_TOAN");
        java.math.BigDecimal tongDaThu = hoaDonService.sumTongDoanhThuDaThu();

        model.addAttribute("pageData", pageData);
        model.addAttribute("countDaThanhToan", countDaThanhToan);
        model.addAttribute("countChuaThanhToan", countChuaThanhToan);
        model.addAttribute("tongDaThu", tongDaThu);
        model.addAttribute("keyword", keyword);
        model.addAttribute("thangNam", thangNam);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sort", "id,desc");
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
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            HoaDon hoaDonMoi = hoaDonService.taoHoaDon(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Lập thành công hóa đơn [" + hoaDonMoi.getMaHoaDon() + "] cho phòng " +
                            (hoaDonMoi.getHopDong().getPhongTro() != null ? hoaDonMoi.getHopDong().getPhongTro().getMaPhong() : "") +
                            ". Tổng tiền: " + String.format("%,.0f", hoaDonMoi.getTongTien()) + " VNĐ");
            return "redirect:/hoa-don";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("danhSachHopDong", hoaDonService.getDanhSachHopDongHieuLuc());
            return "hoa-don/tao-moi";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn: " + ex.getMessage());
            model.addAttribute("danhSachHopDong", hoaDonService.getDanhSachHopDongHieuLuc());
            return "hoa-don/tao-moi";
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
