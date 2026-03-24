package com.yukiani.controller;

import com.yukiani.common.Response;
import com.yukiani.entity.Platform;
import com.yukiani.entity.Season;
import com.yukiani.service.fetch.FetchService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/admin/fetch")
public class AdminFetchController {

    @Resource
    FetchService fetchService;

    @ResponseBody
    @PostMapping("/anime")
    public Response<String> fetchAnime(@RequestParam int year, Season season, Platform platform) {
        fetchService.fetchAnime(year, season, platform);
        return Response.ok("数据抓取任务已启动");
    }

    @ResponseBody
    @PostMapping("/mapping")
    public Response<String> fetchMapping(@RequestParam int year, Season season, Platform platform) {
        fetchService.fetchMapping(year, season, platform);
        return Response.ok("映射抓取任务已启动");
    }

    @ResponseBody
    @PostMapping("/link")
    public Response<String> linkMappings() {
        fetchService.linkMappings();
        return Response.ok("映射合并任务已启动");
    }

    @ResponseBody
    @PostMapping("/calculate_metric")
    public Response<String> calculateMetric() {
        fetchService.calculateAllMetric();
        return Response.ok("评分计算任务已启动");
    }
}
