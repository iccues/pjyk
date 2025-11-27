package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.common.Response;
import com.iccues.metaanimebackend.dto.admin.AdminAnimeDTO;
import com.iccues.metaanimebackend.dto.admin.AnimeCreateRequest;
import com.iccues.metaanimebackend.dto.admin.AnimeUpdateRequest;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.LocalDateRange;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.exception.ResourceNotFoundException;
import com.iccues.metaanimebackend.mapper.AdminAnimeMapper;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.service.SeasonService;
import com.iccues.metaanimebackend.spec.AnimeSpec;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/admin")
public class AdminAnimeController {

    @Resource
    AnimeRepository animeRepository;

    @Resource
    MappingRepository mappingRepository;

    @Resource
    AdminAnimeMapper adminAnimeMapper;

    @Resource
    SeasonService seasonService;

    @ResponseBody
    @GetMapping("/get_anime_list")
    public Response<List<AdminAnimeDTO>> getAnimeList(
            @RequestParam(required = false) ReviewStatus reviewStatus,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Season season) {
        LocalDateRange dateRange = seasonService.getStartDateRange(year, season);

        Specification<Anime> spec = Specification.allOf(
                AnimeSpec.reviewStatusEquals(reviewStatus),
                AnimeSpec.startDateBetween(dateRange),
                AnimeSpec.orderById()
        );
        List<Anime> animeList = animeRepository.findAll(spec);

        List<AdminAnimeDTO> animeDtoList = adminAnimeMapper.toAnimeDtoList(animeList);
        return Response.ok(animeDtoList);
    }

    @ResponseBody
    @PostMapping("/create_anime")
    @Transactional
    public Response<AdminAnimeDTO> createAnime(@Valid @RequestBody AnimeCreateRequest request) {
        Anime anime = adminAnimeMapper.requestToAnime(request);

        Anime savedAnime = animeRepository.save(anime);
        AdminAnimeDTO animeDto = adminAnimeMapper.toAnimeDto(savedAnime);
        return Response.ok(animeDto);
    }

    @ResponseBody
    @PutMapping("/update_anime")
    @Transactional
    public Response<AdminAnimeDTO> updateAnime(@Valid @RequestBody AnimeUpdateRequest request) {
        Anime anime = animeRepository.findById(request.animeId())
                .orElseThrow(() -> new ResourceNotFoundException("Anime", request.animeId()));

        adminAnimeMapper.updateAnimeByRequest(request, anime);

        Anime updatedAnime = animeRepository.save(anime);
        AdminAnimeDTO animeDto = adminAnimeMapper.toAnimeDto(updatedAnime);
        return Response.ok(animeDto);
    }

    @ResponseBody
    @DeleteMapping("/delete_anime/{animeId}")
    @Transactional
    public Response<Void> deleteAnime(@PathVariable Long animeId) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new ResourceNotFoundException("Anime", animeId));

        // 使用 removeMapping 方法安全地解除所有映射的关联
        // 创建副本以避免 ConcurrentModificationException
        List<Mapping> mappingsCopy = new ArrayList<>(anime.getMappings());
        mappingsCopy.forEach(mapping -> {
            anime.removeMapping(mapping);
            mappingRepository.save(mapping);
        });

        // 删除动画
        animeRepository.delete(anime);
        return Response.ok(null);
    }
}
