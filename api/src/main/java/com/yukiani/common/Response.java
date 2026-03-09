package com.yukiani.common;

import lombok.Data;

@Data
public class Response<T> {
    private boolean success;
    private T data;
    private String message;
    private String code;

    public static <T> Response<T> ok(T data) {
        Response<T> response = new Response<>();
        response.success = true;
        response.data = data;
        return response;
    }

    public static <T> Response<T> fail(String code, String message) {
        Response<T> response = new Response<>();
        response.success = false;
        response.code = code;
        response.message = message;
        return response;
    }
}
