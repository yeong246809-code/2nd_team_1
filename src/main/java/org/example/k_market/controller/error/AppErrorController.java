package org.example.k_market.controller.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class AppErrorController implements ErrorController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        int statusCode = statusCode(request);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String message = valueOr(
                request.getAttribute(RequestDispatcher.ERROR_MESSAGE),
                defaultMessage(statusCode)
        );
        String path = valueOr(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI), request.getRequestURI());

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorTitle", title(statusCode));
        model.addAttribute("errorMessage", message);
        model.addAttribute("errorPath", path);
        model.addAttribute("errorTimestamp", LocalDateTime.now().format(FORMATTER));
        model.addAttribute("errorLog", errorLog(statusCode, path, message, exception));

        return "error/error";
    }

    private int statusCode(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status instanceof Integer code) {
            return code;
        }
        return 500;
    }

    private String title(int statusCode) {
        return switch (statusCode) {
            case 400 -> "잘못된 요청입니다.";
            case 401 -> "로그인이 필요합니다.";
            case 403 -> "접근 권한이 없습니다.";
            case 404 -> "페이지를 찾을 수 없습니다.";
            default -> "죄송합니다. 오류가 발생했습니다.";
        };
    }

    private String defaultMessage(int statusCode) {
        return switch (statusCode) {
            case 404 -> "요청하신 페이지가 삭제되었거나 주소가 변경되었을 수 있습니다.";
            case 403 -> "현재 계정으로는 요청한 페이지에 접근할 수 없습니다.";
            default -> "서비스 이용에 불편을 드려 죄송합니다. 잠시 후 다시 시도해주세요.";
        };
    }

    private String errorLog(int statusCode, String path, String message, Throwable exception) {
        StringBuilder log = new StringBuilder();
        log.append("Status: ").append(statusCode).append('\n');
        log.append("Time: ").append(LocalDateTime.now().format(FORMATTER)).append('\n');
        log.append("Path: ").append(path).append('\n');
        log.append("Message: ").append(message).append('\n');
        if (exception != null) {
            log.append('\n').append("Exception: ").append(exception.getClass().getName()).append('\n');
            log.append("Exception Message: ").append(exception.getMessage()).append('\n');
            log.append('\n').append(stackTrace(exception));
        }
        return log.toString();
    }

    private String stackTrace(Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    private String valueOr(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? fallback : text;
    }
}
