package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.annotation.Audit;
import com.iccues.metaanimebackend.common.Response;
import com.iccues.metaanimebackend.dto.admin.AdminAnimeDTO;
import com.iccues.metaanimebackend.dto.admin.AnimeCreateRequest;
import com.iccues.metaanimebackend.dto.admin.AnimeUpdateRequest;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AuditLog;
import com.iccues.metaanimebackend.entity.LocalDateRange;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin")
@Slf4j
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
    @Audit(operation = AuditLog.OperationType.CREATE, entityType = AuditLog.EntityType.ANIME, description = "创建动漫")
    public Response<AdminAnimeDTO> createAnime(@Valid @RequestBody AnimeCreateRequest request) {
        log.info("Creating new anime: {}", request.title().getTitleNative());
        Anime anime = adminAnimeMapper.requestToAnime(request);

        Anime savedAnime = animeRepository.save(anime);
        log.info("Anime created successfully with id: {}", savedAnime.getAnimeId());
        AdminAnimeDTO animeDto = adminAnimeMapper.toAnimeDto(savedAnime);
        return Response.ok(animeDto);
    }

    @ResponseBody
    @PutMapping("/update_anime")
    @Transactional
    @Audit(operation = AuditLog.OperationType.UPDATE, entityType = AuditLog.EntityType.ANIME, description = "更新动漫")
    public Response<AdminAnimeDTO> updateAnime(@Valid @RequestBody AnimeUpdateRequest request) {
        log.info("Updating anime with id: {}", request.animeId());
        Anime anime = animeRepository.findById(request.animeId())
                .orElseThrow(() -> new ResourceNotFoundException("Anime", request.animeId()));

        adminAnimeMapper.updateAnimeByRequest(request, anime);

        Anime updatedAnime = animeRepository.save(anime);
        log.info("Anime updated successfully: {}", updatedAnime.getTitle().getTitleNative());
        AdminAnimeDTO animeDto = adminAnimeMapper.toAnimeDto(updatedAnime);
        return Response.ok(animeDto);
    }

    @ResponseBody
    @DeleteMapping("/delete_anime/{animeId}")
    @Transactional
    @Audit(operation = AuditLog.OperationType.DELETE, entityType = AuditLog.EntityType.ANIME, description = "删除动漫")
    public Response<Void> deleteAnime(@PathVariable Long animeId) {
        log.info("Deleting anime with id: {}", animeId);
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new ResourceNotFoundException("Anime", animeId));

        // 解除所有映射的关联
        int mappingCount = anime.getMappings().size();
        anime.getMappings().forEach(mapping -> {
            mapping.setAnime(null);
            mappingRepository.save(mapping);
        });
        log.debug("Unlinked {} mappings from anime id: {}", mappingCount, animeId);

        // 再删除动画
        animeRepository.deleteById(animeId);
        log.info("Anime deleted successfully: {}", anime.getTitle().getTitleNative());
        return Response.ok(null);
    }
}
