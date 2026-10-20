package com.nhatro.config;

import com.nhatro.entity.TaiKhoan;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER = "SESSION_USER";
    public static final String SESSION_USER_ID = "SESSION_USER_ID";
    public static final String SESSION_USERNAME = "SESSION_USERNAME";
    public static final String SESSION_ROLE = "SESSION_ROLE";
    public static final String SESSION_FULLNAME = "SESSION_FULLNAME";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        // Bỏ qua các đường dẫn công khai
        if (isPublicPath(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        TaiKhoan user = (session != null) ? (TaiKhoan) session.getAttribute(SESSION_USER) : null;

        // Nếu chưa đăng nhập -> chuyển hướng về trang login
        if (user == null) {
            String redirectUrl = path;
            if (request.getQueryString() != null) {
                redirectUrl += "?" + request.getQueryString();
            }
            response.sendRedirect(contextPath + "/login?redirect=" + java.net.URLEncoder.encode(redirectUrl, java.nio.charset.StandardCharsets.UTF_8));
            return false;
        }

        String role = user.getVaiTro();

        // Phân quyền cho module Tài khoản: chỉ ADMIN mới được truy cập
        if (path.startsWith("/tai-khoan") && !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(contextPath + "/phong-tro?error=access_denied");
            return false;
        }

        // Module Phòng trọ & Khách thuê: ADMIN và STAFF
        if ((path.startsWith("/phong-tro") || path.startsWith("/khach-thue")) && "TENANT".equalsIgnoreCase(role)) {
            response.sendRedirect(contextPath + "/tenant/phong?error=access_denied");
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null && request.getSession(false) != null) {
            HttpSession session = request.getSession(false);
            TaiKhoan user = (TaiKhoan) session.getAttribute(SESSION_USER);
            if (user != null) {
                modelAndView.addObject("currentUser", user);
                modelAndView.addObject("currentRole", session.getAttribute(SESSION_ROLE));
                modelAndView.addObject("currentFullName", session.getAttribute(SESSION_FULLNAME));
            }
        }
    }

    private boolean isPublicPath(String path) {
        return path.equals("/") ||
               path.equals("/login") ||
               path.equals("/logout") ||
               path.equals("/error") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/uploads/") ||
               path.startsWith("/webjars/") ||
               path.equals("/favicon.ico");
    }
}
