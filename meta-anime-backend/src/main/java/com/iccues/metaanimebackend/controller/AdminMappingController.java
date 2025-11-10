package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.common.Response;
import com.iccues.metaanimebackend.dto.admin.AdminMappingDTO;
import com.iccues.metaanimebackend.dto.admin.UpdateMappingAnimeRequest;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.mapper.AdminAnimeMapper;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.service.ScoreService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin")
public class AdminMappingController {

    @Resource
    AnimeRepository animeRepository;
    @Resource
    MappingRepository mappingRepository;

    @Resource
    AdminAnimeMapper adminAnimeMapper;

    @Resource
    ScoreService scoreService;

    @ResponseBody
    @GetMapping("/get_unmapped_mapping_list")
    public Response<List<AdminMappingDTO>> getUnmappedMappingList() {
        List<Mapping> mappingList = mappingRepository.findAllByAnimeIsNull();
        List<AdminMappingDTO> mappingDTOList = adminAnimeMapper.toMappingDtoList(mappingList);
        return Response.ok(mappingDTOList);
    }

    /**
     * 更新映射的动画关联
     * @param request 包含 mappingId 和 animeId（null 表示解除关联）
     * @return 更新后的映射信息
     */
    @ResponseBody
    @PutMapping("/update_mapping_anime")
    @Transactional
    public Response<AdminMappingDTO> updateMappingAnime(@RequestBody UpdateMappingAnimeRequest request) {
        // 查找映射
        Mapping mapping = mappingRepository.findById(request.mappingId())
                .orElseThrow(() -> new RuntimeException("映射不存在: " + request.mappingId()));

        // 获取当前关联的动画（如果有）
        Anime currentAnime = mapping.getAnime();

        if (currentAnime != null) {
            currentAnime.removeMapping(mapping);
        }

        if (request.animeId() != null) {
            Anime targetAnime = animeRepository.findById(request.animeId())
                    .orElseThrow(() -> new RuntimeException("动画不存在: " + request.animeId()));
            targetAnime.addMapping(mapping);
        }

        // 保存并返回
        Mapping savedMapping = mappingRepository.save(mapping);
        AdminMappingDTO mappingDTO = adminAnimeMapper.toMappingDto(savedMapping);

        if (currentAnime != null) {
            scoreService.calculateAverageScore(currentAnime);
        }
        if (savedMapping.getAnime() != null) {
            scoreService.calculateAverageScore(savedMapping.getAnime());
        }
        return Response.ok(mappingDTO);
    }
}
