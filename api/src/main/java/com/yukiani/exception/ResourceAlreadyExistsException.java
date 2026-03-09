package com.yukiani.exception;

/**
 * 资源已存在异常
 */
public class ResourceAlreadyExistsException extends BusinessException {

    public ResourceAlreadyExistsException(String resourceType, Object resourceId) {
        super("ALREADY_EXISTS", String.format("%s 已存在: %s", resourceType, resourceId));
    }
}
