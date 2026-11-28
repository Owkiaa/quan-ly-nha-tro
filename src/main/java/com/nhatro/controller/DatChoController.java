package com.nhatro.controller;

import com.nhatro.dto.DatChoRequestDTO;
import com.nhatro.entity.DatCho;
import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.exception.RoomAlreadyBookedException;
import com.nhatro.service.DatChoMonitor;
import com.nhatro.service.DatChoScheduler;
import com.nhatro.service.DatChoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Controller xử lý các yêu cầu Web MVC liên quan đến Quản lý Đặt giữ chỗ phòng trọ.
 * Phụ trách bởi: Bùi Trần Tấn Tài
 */
@Controller
@RequestMapping("/dat-cho")
public class DatChoController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "ngayDat", "hanGiuCho", "trangThai");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9,12}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Autowired
    private DatChoService datChoService;

    @Autowired
    private DatChoScheduler datChoScheduler;

    @Autowired
    private DatChoMonitor datChoMonitor;

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

        // Xử lý tham số sắp xếp (field,direction) an toàn qua ALLOWED_SORT_FIELDS, chống lỗi 500
        String safeSort = "id,desc";
        Sort sortObj = Sort.by(Sort.Direction.DESC, "id");
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            if (ALLOWED_SORT_FIELDS.contains(field)) {
                String dirStr = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) ? "asc" : "desc";
                Sort.Direction direction = "asc".equals(dirStr) ? Sort.Direction.ASC : Sort.Direction.DESC;
                sortObj = Sort.by(direction, field);
                safeSort = field + "," + dirStr;
            }
        }

        Pageable pageable = PageRequest.of(Math.max(page, 0), size > 0 ? size : 10, sortObj);
        Page<DatCho> pageData = datChoService.timKiem(keyword, trangThai, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("trangThai", trangThai != null ? trangThai : "");
        model.addAttribute("sort", safeSort);
        model.addAttribute("activeMenu", "dat-cho");

        // Thống kê nhanh số lượng (tổng độc lập bộ lọc)
        model.addAttribute("totalBookings", datChoService.demTongDatCho());
        model.addAttribute("countDangGiu", datChoService.demDangGiu());
        model.addAttribute("countHetHan", datChoService.demHetHan());
        model.addAttribute("countDaChuyen", datChoService.demDaChuyenHopDong());

        // Thống kê & nhật ký liên kết đa luồng (Web, Socket, @Scheduled) từ Monitor Chương 4
        model.addAttribute("soClientDangKetNoi", datChoMonitor.getSoClientDangKetNoi());
        model.addAttribute("soLuotDatThanhCong", datChoMonitor.getSoLuotDatThanhCong());
        model.addAttribute("soLuotBiTuChoi", datChoMonitor.getSoLuotBiTuChoi());
        model.addAttribute("soPhongGiaiPhongQuaHan", datChoMonitor.getSoPhongGiaiPhongQuaHan());
        model.addAttribute("nhatKyDaLuong", datChoMonitor.getNhatKyDaLuong());

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
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        validateDatChoRequest(requestDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareDatChoFormModel(model);
            return "dat-cho/form";
        }

        try {
            DatCho saved = datChoService.datCho(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt giữ chỗ thành công cho phòng " + saved.getPhongTro().getMaPhong()
                            + " bởi khách " + saved.getKhachThue().getHoTen()
                            + " (Hạn giữ chỗ đến: " + saved.getHanGiuCho() + ").");
            return "redirect:/dat-cho";
        } catch (RoomAlreadyBookedException e) {
            bindingResult.rejectValue("phongId", "booked", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            prepareDatChoFormModel(model);
            return "dat-cho/form";
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            prepareDatChoFormModel(model);
            return "dat-cho/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            prepareDatChoFormModel(model);
            return "dat-cho/form";
        }
    }

    private void prepareDatChoFormModel(Model model) {
        model.addAttribute("danhSachPhongTrong", datChoService.layDanhSachPhongTrong());
        model.addAttribute("activeMenu", "dat-cho");
    }

    private void validateDatChoRequest(DatChoRequestDTO requestDTO, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("phongId") && requestDTO.getPhongId() == null) {
            bindingResult.rejectValue("phongId", "required", "Vui lòng chọn phòng trọ cần giữ chỗ.");
        }

        String hoTen = requestDTO.getHoTen() != null ? requestDTO.getHoTen().trim() : "";
        if (hoTen.isEmpty()) {
            bindingResult.rejectValue("hoTen", "required", "Họ và tên khách thuê không được để trống.");
        } else if (hoTen.length() > 100) {
            bindingResult.rejectValue("hoTen", "maxlength", "Họ và tên không được vượt quá 100 ký tự.");
        }

        String cccd = requestDTO.getCccd() != null ? requestDTO.getCccd().trim() : "";
        if (cccd.isEmpty()) {
            bindingResult.rejectValue("cccd", "required", "Số CCCD không được để trống.");
        } else if (!CCCD_PATTERN.matcher(cccd).matches()) {
            bindingResult.rejectValue("cccd", "invalid", "Số CCCD phải gồm từ 9 đến 12 chữ số.");
        }

        String soDienThoai = requestDTO.getSoDienThoai() != null ? requestDTO.getSoDienThoai().trim() : "";
        if (soDienThoai.isEmpty()) {
            bindingResult.rejectValue("soDienThoai", "required", "Số điện thoại không được để trống.");
        } else if (!PHONE_PATTERN.matcher(soDienThoai).matches()) {
            bindingResult.rejectValue("soDienThoai", "invalid", "Số điện thoại không hợp lệ (10–11 số, bắt đầu bằng 0).");
        }

        String email = requestDTO.getEmail() != null ? requestDTO.getEmail().trim() : "";
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            bindingResult.rejectValue("email", "invalid", "Địa chỉ email không đúng định dạng.");
        }

        if (!bindingResult.hasFieldErrors("soGioGiuCho")
                && (requestDTO.getSoGioGiuCho() == null || requestDTO.getSoGioGiuCho() <= 0)) {
            bindingResult.rejectValue("soGioGiuCho", "invalid", "Thời gian giữ chỗ không hợp lệ.");
        }

        if (requestDTO.getGhiChu() != null && requestDTO.getGhiChu().length() > 255) {
            bindingResult.rejectValue("ghiChu", "maxlength", "Ghi chú không được vượt quá 255 ký tự.");
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
     * Gia hạn thêm thời gian giữ chỗ cho phiếu đặt phòng đang ở trạng thái DANG_GIU.
     */
    @PostMapping("/{id}/gia-han")
    public String giaHanDatCho(@PathVariable Integer id,
                               @RequestParam(defaultValue = "24") int soGioThem,
                               RedirectAttributes redirectAttributes) {
        try {
            DatCho updated = datChoService.giaHanDatCho(id, soGioThem);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã gia hạn giữ chỗ phòng " + updated.getPhongTro().getMaPhong()
                            + " thêm " + soGioThem + " giờ (Hạn mới: " + updated.getHanGiuCho() + ").");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gia hạn giữ chỗ: " + e.getMessage());
        }
        return "redirect:/dat-cho/" + id;
    }

    /**
     * Kích hoạt quét thủ công các bản ghi đặt chỗ quá hạn để giải phóng phòng.
     */
    @PostMapping("/quet-het-han")
    public String quetHetHan(RedirectAttributes redirectAttributes) {
        try {
            int soLuong = datChoScheduler.quetVaGiaiPhongDatChoQuaHan();
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

    /**
     * Chạy trực tiếp mô phỏng đồng bộ Chương 4 (Không đồng bộ Race Condition vs synchronized Monitor Lock).
     */
    @PostMapping("/mo-phong-dong-bo")
    public String moPhongDongBoChuong4(RedirectAttributes redirectAttributes) {
        try {
            String ketQua = datChoMonitor.chayMoPhongDongBoChuong4(5);
            redirectAttributes.addFlashAttribute("successMessage", ketQua);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi chạy mô phỏng đồng bộ Chương 4: " + e.getMessage());
        }
        return "redirect:/dat-cho";
    }
}
