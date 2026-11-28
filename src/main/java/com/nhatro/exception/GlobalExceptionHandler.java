package com.nhatro.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Bộ xử lý ngoại lệ tập trung toàn hệ thống (@ControllerAdvice)
 * Hiển thị trang thông báo lỗi thân thiện khi phát sinh ngoại lệ chưa được bắt tại Controller.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, HttpServletRequest request, Model model) {
        log.warn("Không tìm thấy tài nguyên tại {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorTitle", "Không tìm thấy trang hoặc dữ liệu yêu cầu");
        model.addAttribute("errorMessage", ex instanceof ResourceNotFoundException
                ? ex.getMessage()
                : "Đường dẫn bạn truy cập không tồn tại hoặc đã được di chuyển.");
        model.addAttribute("requestUri", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler({BusinessException.class, RoomAlreadyBookedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(RuntimeException ex, HttpServletRequest request, Model model) {
        log.warn("Lỗi nghiệp vụ tại {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorCode", 400);
        model.addAttribute("errorTitle", "Thao tác không hợp lệ");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, HttpServletRequest request, Model model) {
        log.error("Lỗi hệ thống tại " + request.getRequestURI(), ex);
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorTitle", "Đã xảy ra lỗi trong quá trình xử lý");
        model.addAttribute("errorMessage", ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "Hệ thống gặp sự cố ngoài ý muốn. Vui lòng thử lại hoặc quay về trang chủ.");
        model.addAttribute("requestUri", request.getRequestURI());
        return "error";
    }
}
