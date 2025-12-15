package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.common.Response;
import com.iccues.metaanimebackend.config.OidcConfig;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class PublicConfigController {

    @Resource
    OidcConfig oidc;

    @GetMapping("/oidc")
    public Response<OidcConfig> oidcConfig() {
        return Response.ok(oidc);
    }
}
