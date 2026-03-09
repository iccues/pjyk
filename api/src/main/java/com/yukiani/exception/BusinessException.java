package com.yukiani.exception;

import com.yukiani.common.Response;
import lombok.Getter;

/**
 * 业务逻辑异常
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public Response<Void> toResponse() {
        return Response.fail(code, super.getMessage());
    }
}
