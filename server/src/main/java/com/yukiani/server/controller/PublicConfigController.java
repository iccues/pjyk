package com.yukiani.server.controller;

import com.yukiani.server.common.Response;
import com.yukiani.server.config.OidcConfig;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供无需认证即可读取的前端运行时配置 API。
 */
@RestController
@RequestMapping("/api/config")
public class PublicConfigController {

    @Resource
    OidcConfig oidc;

    /**
     * 获取后台鉴权开关及前端登录所需的 OIDC 公开配置。
     *
     * @return 包含鉴权开关、OIDC issuer 和 clientId 的成功响应
     */
    @GetMapping("/oidc")
    public Response<OidcConfig> oidcConfig() {
        return Response.ok(oidc);
    }
}
