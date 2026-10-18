package com.yukiani.server.exception;

import com.yukiani.server.entity.Platform;

/**
 * 从外部平台获取数据失败异常。
 */
public class FetchFailedException extends BusinessException {

    public FetchFailedException(Platform platform, String platformId) {
        this(platform, platformId, null);
    }

    /**
     * @param cause 导致失败的原始异常，供调用方区分平台故障与数据问题
     */
    public FetchFailedException(Platform platform, String platformId, Throwable cause) {
        super("FETCH_FAILED", "无法从 " + platform.name() + " 获取数据: " + platformId, cause);
    }
}
