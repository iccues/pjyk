package com.yukiani.server.exception;

import com.yukiani.server.common.Response;
import lombok.Getter;

/**
 * 携带稳定业务错误码、可转换为统一响应的异常基类。
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Response<Void> toResponse() {
        return Response.fail(code, super.getMessage());
    }
}
