package com.yukiani.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super("NOT_FOUND", String.format("%s 不存在: %s", resourceType, resourceId));
    }
}
