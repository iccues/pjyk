package com.iccues.metaanimebackend.exception;

import com.iccues.metaanimebackend.entity.Platform;

/**
 * 不支持的平台异常
 */
public class UnsupportedPlatformException extends BusinessException {

    public UnsupportedPlatformException(Platform platform) {
        super("UNSUPPORTED_PLATFORM", String.format("不支持的平台: %s", platform.name()));
    }
}
