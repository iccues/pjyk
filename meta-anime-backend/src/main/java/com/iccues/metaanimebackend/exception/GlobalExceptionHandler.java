package com.iccues.metaanimebackend.exception;

import com.iccues.metaanimebackend.common.Response;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private void logRequestContext(String level, String message, Exception ex) {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String uri = request.getRequestURI();
                String method = request.getMethod();
                String queryString = request.getQueryString();
                String clientIp = getClientIp(request);

                String fullMessage = String.format(
                    "%s - %s %s%s from %s",
                    message,
                    method,
                    uri,
                    queryString != null ? "?" + queryString : "",
                    clientIp
                );

                if ("ERROR".equals(level)) {
                    log.error(fullMessage, ex);
                } else {
                    log.warn(fullMessage);
                }
            } else {
                if ("ERROR".equals(level)) {
                    log.error(message, ex);
                } else {
                    log.warn(message);
                }
            }
        } catch (Exception e) {
            if ("ERROR".equals(level)) {
                log.error(message, ex);
            } else {
                log.warn(message);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<Void>> handleBusinessException(BusinessException ex) {
        logRequestContext("WARN", "Business exception: " + ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.toResponse());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logRequestContext("WARN", "Resource not found: " + ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Response.fail("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        logRequestContext("WARN", "Validation failed: " + message, ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.fail("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Response<Void>> handleOptimisticLockException(ObjectOptimisticLockingFailureException ex) {
        logRequestContext("WARN", "Optimistic lock conflict: " + ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Response.fail("CONFLICT", "数据已被其他用户修改，请刷新后重试"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logRequestContext("WARN", "Illegal argument: " + ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.fail("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleException(Exception ex) {
        logRequestContext("ERROR", "Unexpected error: " + ex.getClass().getSimpleName(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.fail("INTERNAL_ERROR", "服务器内部错误"));
    }
}
