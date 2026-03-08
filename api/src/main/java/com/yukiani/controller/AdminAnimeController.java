package com.yukiani.controller;

import com.yukiani.common.Response;
import com.yukiani.dto.admin.AdminAnimeDTO;
import com.yukiani.dto.admin.AnimeCreateRequest;
import com.yukiani.dto.admin.AnimeUpdateRequest;
import com.yukiani.entity.Anime;
import com.yukiani.entity.ReviewStatus;
import com.yukiani.entity.Season;
import com.yukiani.mapper.AdminAnimeMapper;
import com.yukiani.service.AnimeManageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin")
public class AdminAnimeController {

    @Resource
    AnimeManageService animeManageService;

    @Resource
    AdminAnimeMapper adminAnimeMapper;

    @ResponseBody
    @GetMapping("/get_anime_list")
    public Response<List<AdminAnimeDTO>> getAnimeList(
            @RequestParam(required = false) ReviewStatus reviewStatus,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Season season) {
        List<Anime> animeList = animeManageService.getAnimeList(reviewStatus, year, season);
        List<AdminAnimeDTO> animeDtoList = adminAnimeMapper.toAnimeDtoList(animeList);
        return Response.ok(animeDtoList);
    }

    @ResponseBody
    @PostMapping("/create_anime")
    public Response<AdminAnimeDTO> createAnime(@Valid @RequestBody AnimeCreateRequest request) {
        Anime anime = adminAnimeMapper.requestToAnime(request);
        Anime savedAnime = animeManageService.createAnime(anime);
        AdminAnimeDTO animeDto = adminAnimeMapper.toAnimeDto(savedAnime);
        return Response.ok(animeDto);
    }

    @ResponseBody
    @PutMapping("/update_anime")
    public Response<AdminAnimeDTO> updateAnime(@Valid @RequestBody AnimeUpdateRequest request) {
        Anime updatedAnime = animeManageService.updateAnime(request.animeId(),
                anime -> adminAnimeMapper.updateAnimeByRequest(request, anime));
        AdminAnimeDTO animeDto = adminAnimeMapper.toAnimeDto(updatedAnime);
        return Response.ok(animeDto);
    }

    @ResponseBody
    @DeleteMapping("/delete_anime/{animeId}")
    public Response<Void> deleteAnime(@PathVariable Long animeId) {
        animeManageService.deleteAnime(animeId);
        return Response.ok(null);
    }

    @ResponseBody
    @DeleteMapping("/delete_non_approved_animes")
    public Response<Void> deleteNonApprovedAnimes() {
        animeManageService.deleteNonApprovedAnimes();
        return Response.ok(null);
    }
}
