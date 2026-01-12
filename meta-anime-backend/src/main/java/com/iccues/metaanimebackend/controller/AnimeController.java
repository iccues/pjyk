package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.common.Response;
import com.iccues.metaanimebackend.dto.AnimeDTO;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.entity.SortBy;
import com.iccues.metaanimebackend.mapper.AnimeMapper;
import com.iccues.metaanimebackend.service.AnimeQueryService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/anime")
class AnimeController {

    @Resource
    AnimeQueryService animeQueryService;

    @Resource
    AnimeMapper animeMapper;

    @ResponseBody
    @GetMapping("/get_list")
    public Response<Page<AnimeDTO>> getAnimeList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Season season,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(defaultValue = "SCORE") SortBy sortBy) {

        Page<Anime> animePage = animeQueryService.getAnimeList(year, season, page, pageSize, sortBy);
        Page<AnimeDTO> dtoPage = animePage.map(animeMapper::toDto);
        return Response.ok(dtoPage);
    }
}
