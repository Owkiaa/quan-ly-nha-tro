package com.nhatro.controller;

import com.nhatro.config.AuthInterceptor;
import com.nhatro.dto.IncidentForm;
import com.nhatro.entity.DatCho;
import com.nhatro.entity.HoaDon;
import com.nhatro.entity.HopDong;
import com.nhatro.entity.PhongTro;
import com.nhatro.service.TenantPortalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Cổng Khách thuê (TV3): tra cứu phòng trống, giữ chỗ trực tuyến, hợp đồng, hóa đơn và phản ánh sự cố.
 *
 * <p>Cô lập dữ liệu: {@code khachId} CHỈ lấy từ session (do TV1 lưu sau khi đăng nhập), không bao giờ nhận từ
 * tham số URL hay form, nên khách không thể xem/sửa dữ liệu của người khác bằng cách đổi id trên đường dẫn.
 */
@Controller
@RequestMapping("/tenant")
public class TenantPortalController {

    /**
     * >>> CHỖ DUY NHẤT phụ thuộc cách đăng nhập của TV1.
     * Sau khi đăng nhập, TV1 lưu id hồ sơ khách (tai_khoan.khach_id) vào session với tên này
     * (đặt theo kiểu SESSION_FULLNAME, SESSION_ROLE của fragments.html).
     * Nếu TV1 đặt tên khác (ví dụ "khachId"), chỉ cần sửa hằng số này.
     */
    private static final String SESSION_KHACH_ID = AuthInterceptor.SESSION_KHACH_ID;
    private static final DateTimeFormatter GIO_PHUT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final TenantPortalService service;

    public TenantPortalController(TenantPortalService service) {
        this.service = service;
    }

    private Integer currentKhachId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_KHACH_ID);
        return (value instanceof Number) ? ((Number) value).intValue() : null;
    }

    private boolean isLoggedIn(HttpSession session) {
        return session != null && (session.getAttribute(AuthInterceptor.SESSION_USER) != null
                || currentKhachId(session) != null);
    }

    private String redirectMissingKhachProfile(HttpSession session, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        redirect.addFlashAttribute("infoMessage",
                "Tài khoản hiện tại chưa liên kết hồ sơ khách thuê nên chỉ xem được danh sách phòng trống.");
        return "redirect:/tenant/rooms";
    }

    /** Fragment pagination của TV1 nhận keyword, trangThai, sort: đổi null thành chuỗi rỗng cho link sạch. */
    private static String nz(String s) {
        return s == null ? "" : s;
    }

    // ------------------------------------------------------------ Phòng trống

    /**
     * Lọc nâng cao (tất cả đều tùy chọn): keyword, loaiPhong, giaTu, giaDen, dienTichToiThieu.
     * Các giá trị lọc được đưa lại ra Model để form giữ nguyên lựa chọn của khách.
     */
    @GetMapping("/rooms")
    public String rooms(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String loaiPhong,
                        @RequestParam(required = false) BigDecimal giaTu,
                        @RequestParam(required = false) BigDecimal giaDen,
                        @RequestParam(required = false) Double dienTichToiThieu,
                        @RequestParam(required = false) String sort,
                        @PageableDefault(size = 8, sort = "giaThue") Pageable pageable,
                        HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        Page<PhongTro> page = service.searchAvailableRooms(keyword, loaiPhong, giaTu, giaDen,
                dienTichToiThieu, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", nz(keyword));
        model.addAttribute("loaiPhong", nz(loaiPhong));
        model.addAttribute("giaTu", giaTu);
        model.addAttribute("giaDen", giaDen);
        model.addAttribute("dienTichToiThieu", dienTichToiThieu);
        model.addAttribute("loaiPhongOptions", service.getRoomTypes());
        model.addAttribute("trangThai", "");
        model.addAttribute("sort", nz(sort));
        return "tenant/rooms";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@PathVariable Integer id, HttpSession session,
                             Model model, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        Optional<PhongTro> room = service.findRoom(id);
        if (room.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy phòng bạn yêu cầu.");
            return "redirect:/tenant/rooms";
        }
        model.addAttribute("room", room.get());
        return "tenant/room-detail";
    }

    // ------------------------------------------------------------ Đặt giữ chỗ trực tuyến

    /** Giữ chỗ phòng {id} cho khách đang đăng nhập (khachId lấy từ session, không nhận từ request). */
    @PostMapping("/rooms/{id}/book")
    public String bookRoom(@PathVariable Integer id,
                           @RequestParam(required = false) Integer soGioGiuCho,
                           @RequestParam(required = false) String ghiChu,
                           HttpSession session, RedirectAttributes redirect) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return redirectMissingKhachProfile(session, redirect);
        }
        try {
            DatCho datCho = service.bookRoomForTenant(khachId, id, soGioGiuCho, ghiChu);
            String han = (datCho != null && datCho.getHanGiuCho() != null)
                    ? " Phiếu có hiệu lực đến " + datCho.getHanGiuCho().format(GIO_PHUT) + "."
                    : "";
            redirect.addFlashAttribute("successMessage", "Đã giữ chỗ phòng thành công." + han);
            return "redirect:/tenant/rooms";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/tenant/rooms/" + id;
        }
    }

    /** Hủy phiếu giữ chỗ {id}; chỉ hủy được phiếu của chính khách đang đăng nhập. */
    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return redirectMissingKhachProfile(session, redirect);
        }
        try {
            service.cancelBookingByKhach(khachId, id);
            redirect.addFlashAttribute("successMessage", "Đã hủy phiếu giữ chỗ.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tenant/rooms";
    }

    // ------------------------------------------------------------ Hợp đồng

    @GetMapping("/contracts")
    public String contracts(HttpSession session, Model model, RedirectAttributes redirect) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return redirectMissingKhachProfile(session, redirect);
        }
        model.addAttribute("contracts", service.getContracts(khachId));
        return "tenant/my-contracts";
    }

    // ------------------------------------------------------------ Hóa đơn

    @GetMapping("/invoices")
    public String invoices(@RequestParam(required = false) String trangThai,
                           @RequestParam(required = false) String sort,
                           @PageableDefault(size = 10, sort = "ngayLap", direction = Sort.Direction.DESC) Pageable pageable,
                           HttpSession session, Model model, RedirectAttributes redirect) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return redirectMissingKhachProfile(session, redirect);
        }
        Page<HoaDon> page = service.getInvoices(khachId, trangThai, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", "");
        model.addAttribute("trangThai", nz(trangThai));
        model.addAttribute("sort", nz(sort));
        model.addAttribute("unpaidCount", service.countUnpaidInvoices(khachId));
        model.addAttribute("unpaidTotal", service.sumUnpaidInvoices(khachId));
        return "tenant/invoices";
    }

    @GetMapping("/invoices/{id}")
    public String invoiceDetail(@PathVariable Integer id, HttpSession session,
                                Model model, RedirectAttributes redirect) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return redirectMissingKhachProfile(session, redirect);
        }
        Optional<HoaDon> invoice = service.findInvoice(khachId, id);
        if (invoice.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy hóa đơn bạn yêu cầu.");
            return "redirect:/tenant/invoices";
        }
        model.addAttribute("invoice", invoice.get());
        return "tenant/invoice-detail";
    }

    // ------------------------------------------------------------ Sự cố

    @GetMapping("/incidents/new")
    public String incidentForm(HttpSession session, Model model, RedirectAttributes redirect) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return redirectMissingKhachProfile(session, redirect);
        }
        IncidentForm form = new IncidentForm();
        List<HopDong> active = fillIncidentPage(model, khachId);
        if (active.size() == 1) {
            form.setPhongId(active.get(0).getPhongTro().getId());   // chỉ có 1 phòng: chọn sẵn
        }
        model.addAttribute("form", form);
        return "tenant/incident-form";
    }

    @PostMapping("/incidents")
    public String createIncident(@ModelAttribute("form") IncidentForm form, BindingResult result,
                                 HttpSession session, RedirectAttributes redirect, Model model) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return redirectMissingKhachProfile(session, redirect);
        }

        if (form.getPhongId() == null) {
            result.rejectValue("phongId", "required", "Vui lòng chọn phòng gặp sự cố.");
        }
        String noiDung = form.getNoiDung() == null ? "" : form.getNoiDung().trim();
        if (noiDung.isEmpty()) {
            result.rejectValue("noiDung", "required", "Vui lòng mô tả sự cố.");
        } else if (noiDung.length() < 10) {
            result.rejectValue("noiDung", "short", "Hãy mô tả rõ hơn (ít nhất 10 ký tự) để thợ biết cần sửa gì.");
        } else if (noiDung.length() > TenantPortalService.NOI_DUNG_TOI_DA) {
            result.rejectValue("noiDung", "long",
                    "Mô tả tối đa " + TenantPortalService.NOI_DUNG_TOI_DA + " ký tự.");
        }
        if (form.getMucDo() == null || !TenantPortalService.MUC_DO_HOP_LE.contains(form.getMucDo())) {
            result.rejectValue("mucDo", "invalid", "Mức độ không hợp lệ.");
        }

        if (!result.hasErrors()) {
            try {
                service.createIncident(khachId, form.getPhongId(), noiDung, form.getMucDo());
                redirect.addFlashAttribute("successMessage",
                        "Đã gửi phản ánh. Quản lý sẽ tiếp nhận sớm nhất có thể.");
                return "redirect:/tenant/incidents/new";
            } catch (IllegalArgumentException ex) {
                result.rejectValue("phongId", "invalid", ex.getMessage());
            }
        }

        fillIncidentPage(model, khachId);
        return "tenant/incident-form";
    }

    private List<HopDong> fillIncidentPage(Model model, Integer khachId) {
        List<HopDong> active = service.getActiveContracts(khachId);
        model.addAttribute("activeContracts", active);
        model.addAttribute("incidents", service.getIncidents(khachId));
        return active;
    }
}
