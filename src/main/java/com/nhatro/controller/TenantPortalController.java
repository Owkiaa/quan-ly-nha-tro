package com.nhatro.controller;

import com.nhatro.dto.IncidentForm;
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

import java.util.List;
import java.util.Optional;

/** Cổng Khách thuê (TV3): tra cứu phòng trống, hợp đồng, hóa đơn và gửi phản ánh sự cố. */
@Controller
@RequestMapping("/tenant")
public class TenantPortalController {

    /**
     * >>> CHỖ DUY NHẤT phụ thuộc cách đăng nhập của TV1.
     * Sau khi đăng nhập, TV1 lưu id hồ sơ khách (tai_khoan.khach_id) vào session với tên này
     * (đặt theo kiểu SESSION_FULLNAME, SESSION_ROLE của fragments.html).
     * Nếu TV1 đặt tên khác, chỉ cần sửa hằng số này.
     */
    private static final String SESSION_KHACH_ID = "SESSION_KHACH_ID";

    private final TenantPortalService service;

    public TenantPortalController(TenantPortalService service) {
        this.service = service;
    }

    private Integer currentKhachId(HttpSession session) {
        Object value = session.getAttribute(SESSION_KHACH_ID);
        return (value instanceof Number) ? ((Number) value).intValue() : null;
    }

    /** Fragment pagination của TV1 nhận keyword, trangThai, sort: đổi null thành chuỗi rỗng cho link sạch. */
    private static String nz(String s) {
        return s == null ? "" : s;
    }

    // ------------------------------------------------------------ Phòng trống

    @GetMapping("/rooms")
    public String rooms(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String sort,
                        @PageableDefault(size = 8, sort = "giaThue") Pageable pageable,
                        HttpSession session, Model model) {
        if (currentKhachId(session) == null) {
            return "redirect:/login";
        }
        Page<PhongTro> page = service.searchAvailableRooms(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", nz(keyword));
        model.addAttribute("trangThai", "");
        model.addAttribute("sort", nz(sort));
        return "tenant/rooms";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@PathVariable Integer id, HttpSession session,
                             Model model, RedirectAttributes redirect) {
        if (currentKhachId(session) == null) {
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

    // ------------------------------------------------------------ Hợp đồng

    @GetMapping("/contracts")
    public String contracts(HttpSession session, Model model) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return "redirect:/login";
        }
        model.addAttribute("contracts", service.getContracts(khachId));
        return "tenant/my-contracts";
    }

    // ------------------------------------------------------------ Hóa đơn

    @GetMapping("/invoices")
    public String invoices(@RequestParam(required = false) String trangThai,
                           @RequestParam(required = false) String sort,
                           @PageableDefault(size = 10, sort = "ngayLap", direction = Sort.Direction.DESC) Pageable pageable,
                           HttpSession session, Model model) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return "redirect:/login";
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
            return "redirect:/login";
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
    public String incidentForm(HttpSession session, Model model) {
        Integer khachId = currentKhachId(session);
        if (khachId == null) {
            return "redirect:/login";
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
            return "redirect:/login";
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
