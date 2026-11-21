package com.nhatro.controller;

import com.nhatro.dto.HopDongRequestDTO;
import com.nhatro.entity.DatCho;
import com.nhatro.entity.HopDong;
import com.nhatro.entity.KhachThue;
import com.nhatro.entity.PhongTro;
import com.nhatro.exception.BusinessException;
import com.nhatro.repository.PhongTroRepository;
import com.nhatro.service.HopDongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý các yêu cầu Web MVC cho phân hệ Quản lý Hợp đồng thuê phòng.
 * Phụ trách bởi: Bùi Trần Tấn Tài
 */
@Controller
@RequestMapping("/hop-dong")
public class HopDongController {

    @Autowired
    private HopDongService hopDongService;

    @Autowired
    private PhongTroRepository phongTroRepository;

    /**
     * Danh sách hợp đồng thuê phòng với tìm kiếm, lọc theo trạng thái, sắp xếp và phân trang.
     */
    @GetMapping({"", "/"})
    public String index(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String trangThai,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "id,desc") String sort,
                        Model model) {

        Sort sortObj = Sort.by(Sort.Direction.DESC, "id");
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            String field = parts[0];
            Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            sortObj = Sort.by(direction, field);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<HopDong> pageData = hopDongService.timKiem(keyword, trangThai, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("trangThai", trangThai != null ? trangThai : "");
        model.addAttribute("sort", sort);
        model.addAttribute("activeMenu", "hop-dong");

        // Số liệu KPI
        model.addAttribute("countHieuLuc", hopDongService.demHieuLuc());
        model.addAttribute("countHetHan", hopDongService.demHetHan());
        model.addAttribute("countDaThanhLy", hopDongService.demDaThanhLy());

        return "hop-dong/index";
    }

    /**
     * Form lập hợp đồng thuê phòng mới.
     * Tự động nhận diện và pre-fill dữ liệu nếu chuyển tiếp từ Đặt giữ chỗ hoặc chọn phòng cụ thể.
     */
    @GetMapping("/them-moi")
    public String hienThiFormThemMoi(@RequestParam(required = false) Integer phongId,
                                    @RequestParam(required = false) Integer khachId,
                                    Model model) {
        HopDongRequestDTO requestDTO = new HopDongRequestDTO();
        requestDTO.setNgayBatDau(LocalDate.now());
        requestDTO.setNgayKetThuc(LocalDate.now().plusMonths(6));
        requestDTO.setChuKyThanhToan(1);

        if (phongId != null) {
            requestDTO.setPhongId(phongId);
            phongTroRepository.findById(phongId).ifPresent(p -> {
                requestDTO.setGiaThueThoaThuan(p.getGiaThue());
                requestDTO.setTienDatCoc(p.getGiaThue()); // Mặc định cọc 1 tháng tiền phòng

                // Nếu phòng đang DA_DAT, tìm khách đã đặt giữ chỗ để tự động gắn vào form
                Optional<DatCho> optDatCho = hopDongService.timDatChoDangGiuCuaPhong(phongId);
                optDatCho.ifPresent(dc -> {
                    requestDTO.setKhachId(dc.getKhachThue().getId());
                    model.addAttribute("thongTinDatCho", dc);
                });
            });
        }

        if (khachId != null) {
            requestDTO.setKhachId(khachId);
        }

        List<PhongTro> danhSachPhong = hopDongService.layDanhSachPhongCoTheLapHopDong();
        List<KhachThue> danhSachKhach = hopDongService.layDanhSachKhachThue();

        model.addAttribute("hopDongRequest", requestDTO);
        model.addAttribute("danhSachPhong", danhSachPhong);
        model.addAttribute("danhSachKhach", danhSachKhach);
        model.addAttribute("activeMenu", "hop-dong");

        return "hop-dong/form";
    }

    /**
     * Lưu hợp đồng thuê phòng mới.
     */
    @PostMapping("/luu")
    public String luuHopDong(@ModelAttribute("hopDongRequest") HopDongRequestDTO requestDTO,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        validateHopDongRequest(requestDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareHopDongFormModel(model, requestDTO.getPhongId());
            return "hop-dong/form";
        }

        try {
            HopDong saved = hopDongService.taoHopDong(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Lập hợp đồng thành công! Mã hợp đồng: " + saved.getMaHopDong()
                            + " (Phòng: " + saved.getPhongTro().getMaPhong()
                            + ", Khách: " + saved.getKhachThue().getHoTen() + ").");
            return "redirect:/hop-dong/" + saved.getId();
        } catch (BusinessException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Không thể lập hợp đồng.";
            if (msg.contains("giữ chỗ")) {
                bindingResult.rejectValue("khachId", "mismatch", msg);
            } else if (msg.contains("Mã hợp đồng")) {
                bindingResult.rejectValue("maHopDong", "duplicate", msg);
            }
            model.addAttribute("errorMessage", msg);
            prepareHopDongFormModel(model, requestDTO.getPhongId());
            return "hop-dong/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tạo hợp đồng: " + e.getMessage());
            prepareHopDongFormModel(model, requestDTO.getPhongId());
            return "hop-dong/form";
        }
    }

    private void prepareHopDongFormModel(Model model, Integer phongId) {
        if (phongId != null) {
            hopDongService.timDatChoDangGiuCuaPhong(phongId)
                    .ifPresent(dc -> model.addAttribute("thongTinDatCho", dc));
        }
        model.addAttribute("danhSachPhong", hopDongService.layDanhSachPhongCoTheLapHopDong());
        model.addAttribute("danhSachKhach", hopDongService.layDanhSachKhachThue());
        model.addAttribute("activeMenu", "hop-dong");
    }

    private void validateHopDongRequest(HopDongRequestDTO requestDTO, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("phongId") && requestDTO.getPhongId() == null) {
            bindingResult.rejectValue("phongId", "required", "Vui lòng chọn phòng trọ.");
        }
        if (!bindingResult.hasFieldErrors("khachId") && requestDTO.getKhachId() == null) {
            bindingResult.rejectValue("khachId", "required", "Vui lòng chọn khách thuê đứng tên hợp đồng.");
        }
        if (!bindingResult.hasFieldErrors("ngayBatDau") && requestDTO.getNgayBatDau() == null) {
            bindingResult.rejectValue("ngayBatDau", "required", "Ngày bắt đầu hợp đồng không được để trống.");
        }
        if (!bindingResult.hasFieldErrors("ngayKetThuc") && requestDTO.getNgayKetThuc() == null) {
            bindingResult.rejectValue("ngayKetThuc", "required", "Ngày kết thúc hợp đồng không được để trống.");
        }
        if (!bindingResult.hasFieldErrors("ngayBatDau") && !bindingResult.hasFieldErrors("ngayKetThuc")
                && requestDTO.getNgayBatDau() != null && requestDTO.getNgayKetThuc() != null
                && !requestDTO.getNgayKetThuc().isAfter(requestDTO.getNgayBatDau())) {
            bindingResult.rejectValue("ngayKetThuc", "invalid", "Ngày kết thúc hợp đồng phải sau ngày bắt đầu.");
        }
        if (!bindingResult.hasFieldErrors("chuKyThanhToan")
                && (requestDTO.getChuKyThanhToan() == null || requestDTO.getChuKyThanhToan() <= 0)) {
            bindingResult.rejectValue("chuKyThanhToan", "invalid", "Chu kỳ thanh toán không hợp lệ.");
        }
        if (!bindingResult.hasFieldErrors("giaThueThoaThuan")
                && (requestDTO.getGiaThueThoaThuan() == null || requestDTO.getGiaThueThoaThuan().compareTo(BigDecimal.ZERO) <= 0)) {
            bindingResult.rejectValue("giaThueThoaThuan", "invalid", "Giá thuê thỏa thuận phải lớn hơn 0.");
        }
        if (!bindingResult.hasFieldErrors("tienDatCoc")
                && (requestDTO.getTienDatCoc() == null || requestDTO.getTienDatCoc().compareTo(BigDecimal.ZERO) < 0)) {
            bindingResult.rejectValue("tienDatCoc", "invalid", "Tiền đặt cọc không được nhỏ hơn 0.");
        }
        if (requestDTO.getMaHopDong() != null && !requestDTO.getMaHopDong().trim().isEmpty()) {
            String maHopDong = requestDTO.getMaHopDong().trim();
            if (maHopDong.length() > 30) {
                bindingResult.rejectValue("maHopDong", "maxlength", "Mã hợp đồng không được vượt quá 30 ký tự.");
            } else if (hopDongService.timTheoMaHopDong(maHopDong).isPresent()) {
                bindingResult.rejectValue("maHopDong", "duplicate", "Mã hợp đồng đã tồn tại trong hệ thống.");
            }
        }
    }

    /**
     * Xem thông tin chi tiết một hợp đồng thuê phòng.
     */
    @GetMapping("/{id}")
    public String xemChiTiet(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        return hopDongService.timTheoId(id)
                .map(hd -> {
                    model.addAttribute("hopDong", hd);
                    model.addAttribute("activeMenu", "hop-dong");
                    return "hop-dong/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hợp đồng với ID: " + id);
                    return "redirect:/hop-dong";
                });
    }

    /**
     * Gia hạn hợp đồng thuê phòng.
     */
    @PostMapping("/{id}/gia-han")
    public String giaHanHopDong(@PathVariable Integer id,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKetThucMoi,
                                RedirectAttributes redirectAttributes) {
        try {
            HopDong hd = hopDongService.giaHanHopDong(id, ngayKetThucMoi);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Gia hạn hợp đồng " + hd.getMaHopDong() + " thành công đến ngày " + ngayKetThucMoi + ".");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gia hạn hợp đồng: " + e.getMessage());
        }
        return "redirect:/hop-dong/" + id;
    }

    /**
     * Thanh lý hợp đồng thuê phòng và tự động giải phóng phòng về TRONG.
     */
    @PostMapping("/{id}/thanh-ly")
    public String thanhLyHopDong(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            HopDong hd = hopDongService.thanhLyHopDong(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã thanh lý thành công hợp đồng " + hd.getMaHopDong()
                            + ". Phòng " + hd.getPhongTro().getMaPhong() + " đã được trả về trạng thái TRỐNG.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thanh lý hợp đồng: " + e.getMessage());
        }
        return "redirect:/hop-dong";
    }

    /**
     * Xóa hợp đồng thuê phòng (tuân thủ nghiêm ngặt ràng buộc chặn xóa nếu đã có hóa đơn).
     */
    @PostMapping("/{id}/xoa")
    public String xoaHopDong(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            hopDongService.xoaHopDong(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa hợp đồng thành công.");
        } catch (BusinessException e) {
            // Ném đúng thông báo chuẩn: "Không thể xóa hợp đồng đã phát sinh hóa đơn thu tiền..."
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa hợp đồng: " + e.getMessage());
        }
        return "redirect:/hop-dong";
    }
}
