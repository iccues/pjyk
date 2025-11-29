package com.iccues.metaanimebackend.exception;

import com.iccues.metaanimebackend.entity.Platform;

/**
 * 从外部平台获取数据失败异常
 */
public class FetchFailedException extends BusinessException {

    public FetchFailedException(Platform platform, String platformId) {
        super("FETCH_FAILED", "无法从 " + platform.name() + " 获取数据: " + platformId);
    }
}
