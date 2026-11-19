package com.nhatro.controller;

import com.nhatro.dto.DatChoRequestDTO;
import com.nhatro.entity.DatCho;
import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.RoomAlreadyBookedException;
import com.nhatro.service.DatChoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller xử lý các yêu cầu Web MVC liên quan đến Quản lý Đặt giữ chỗ phòng trọ.
 * Phụ trách bởi: Bùi Trần Tấn Tài
 */
@Controller
@RequestMapping("/dat-cho")
public class DatChoController {

    @Autowired
    private DatChoService datChoService;

    /**
     * Hiển thị danh sách đặt giữ chỗ có tìm kiếm, lọc theo trạng thái, sắp xếp và phân trang.
     */
    @GetMapping({"", "/"})
    public String index(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String trangThai,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "id,desc") String sort,
                        Model model) {

        // Xử lý tham số sắp xếp (field,direction)
        Sort sortObj = Sort.by(Sort.Direction.DESC, "id");
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            String field = parts[0];
            Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            sortObj = Sort.by(direction, field);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<DatCho> pageData = datChoService.timKiem(keyword, trangThai, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("trangThai", trangThai != null ? trangThai : "");
        model.addAttribute("sort", sort);
        model.addAttribute("activeMenu", "dat-cho");

        // Thống kê nhanh số lượng
        model.addAttribute("countDangGiu", datChoService.demDangGiu());
        model.addAttribute("countHetHan", datChoService.demHetHan());
        model.addAttribute("countDaChuyen", datChoService.demDaChuyenHopDong());

        return "dat-cho/index";
    }

    /**
     * Màn hình Form tạo mới yêu cầu đặt giữ chỗ phòng.
     */
    @GetMapping("/them-moi")
    public String hienThiFormThemMoi(@RequestParam(required = false) Integer phongId, Model model) {
        DatChoRequestDTO requestDTO = new DatChoRequestDTO();
        if (phongId != null) {
            requestDTO.setPhongId(phongId);
        }

        List<PhongTro> danhSachPhongTrong = datChoService.layDanhSachPhongTrong();

        model.addAttribute("datChoRequest", requestDTO);
        model.addAttribute("danhSachPhongTrong", danhSachPhongTrong);
        model.addAttribute("activeMenu", "dat-cho");

        return "dat-cho/form";
    }

    /**
     * Xử lý lưu đặt giữ chỗ phòng mới từ Web Form.
     */
    @PostMapping("/luu")
    public String luuDatCho(@ModelAttribute("datChoRequest") DatChoRequestDTO requestDTO,
                            RedirectAttributes redirectAttributes) {
        try {
            DatCho saved = datChoService.datCho(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt giữ chỗ thành công cho phòng " + saved.getPhongTro().getMaPhong()
                            + " bởi khách " + saved.getKhachThue().getHoTen()
                            + " (Hạn giữ chỗ đến: " + saved.getHanGiuCho() + ").");
            return "redirect:/dat-cho";
        } catch (RoomAlreadyBookedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dat-cho/them-moi" + (requestDTO.getPhongId() != null ? "?phongId=" + requestDTO.getPhongId() : "");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dat-cho/them-moi" + (requestDTO.getPhongId() != null ? "?phongId=" + requestDTO.getPhongId() : "");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/dat-cho/them-moi";
        }
    }

    /**
     * Xem thông tin chi tiết một bản ghi đặt chỗ.
     */
    @GetMapping("/{id}")
    public String xemChiTiet(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        return datChoService.timTheoId(id)
                .map(datCho -> {
                    model.addAttribute("datCho", datCho);
                    model.addAttribute("activeMenu", "dat-cho");
                    return "dat-cho/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đặt chỗ với ID: " + id);
                    return "redirect:/dat-cho";
                });
    }

    /**
     * Hủy giữ chỗ thủ công và tự động trả phòng về trạng thái TRONG.
     */
    @PostMapping("/{id}/huy")
    public String huyDatCho(@PathVariable Integer id,
                            @RequestParam(required = false, defaultValue = "Khách hàng yêu cầu hủy giữ chỗ") String lyDoHuy,
                            RedirectAttributes redirectAttributes) {
        try {
            datChoService.huyDatCho(id, lyDoHuy);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy giữ chỗ thành công. Phòng trọ đã được đưa trở lại trạng thái TRỐNG.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hủy giữ chỗ: " + e.getMessage());
        }
        return "redirect:/dat-cho";
    }

    /**
     * Kích hoạt quét thủ công các bản ghi đặt chỗ quá hạn để giải phóng phòng.
     */
    @PostMapping("/quet-het-han")
    public String quetHetHan(RedirectAttributes redirectAttributes) {
        try {
            int soLuong = datChoService.quetVaGiaiPhongDatChoQuaHan();
            if (soLuong > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã quét và tự động giải phóng thành công " + soLuong + " phòng quá hạn giữ chỗ về trạng thái TRỐNG.");
            } else {
                redirectAttributes.addFlashAttribute("infoMessage", "Hiện không có phòng nào bị quá hạn giữ chỗ.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thực hiện quét đặt chỗ quá hạn: " + e.getMessage());
        }
        return "redirect:/dat-cho";
    }
}
