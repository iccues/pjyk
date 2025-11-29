package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.common.Response;
import com.iccues.metaanimebackend.dto.admin.AdminMappingDTO;
import com.iccues.metaanimebackend.dto.admin.CreateMappingRequest;
import com.iccues.metaanimebackend.dto.admin.UpdateMappingAnimeRequest;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.exception.ResourceAlreadyExistsException;
import com.iccues.metaanimebackend.exception.ResourceNotFoundException;
import com.iccues.metaanimebackend.mapper.AdminAnimeMapper;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.service.ScoreService;
import com.iccues.metaanimebackend.service.fetch.AbstractAnimeFetchService;
import com.iccues.metaanimebackend.service.fetch.FetchService;
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

    @Resource
    FetchService fetchService;

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
                .orElseThrow(() -> new ResourceNotFoundException("Mapping", request.mappingId()));

        // 获取当前关联的动画（如果有）
        Anime currentAnime = mapping.getAnime();

        if (currentAnime != null) {
            currentAnime.removeMapping(mapping);
        }

        if (request.animeId() != null) {
            Anime targetAnime = animeRepository.findById(request.animeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Anime", request.animeId()));
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

    @ResponseBody
    @DeleteMapping("/delete_mapping/{mappingId}")
    @Transactional
    public Response<Void> deleteMapping(@PathVariable Long mappingId) {
        // 查找映射
        Mapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping", mappingId));

        // 获取关联的动画（如果有）
        Anime relatedAnime = mapping.getAnime();

        // 如果关联了动画，先解除关联并保存
        if (relatedAnime != null) {
            relatedAnime.removeMapping(mapping);
            // 注意：必须在删除 mapping 之前保存 anime，确保双向关系正确更新
            animeRepository.save(relatedAnime);
        }

        // 删除映射
        mappingRepository.delete(mapping);

        // 如果之前关联了动画，重新计算该动画的平均分
        if (relatedAnime != null) {
            scoreService.calculateAverageScore(relatedAnime);
        }

        return Response.ok(null);
    }

    /**
     * 从指定平台创建新的映射
     * @param request 包含 sourcePlatform 和 platformId
     * @return 创建的映射信息
     */
    @ResponseBody
    @PostMapping("/create_mapping")
    public Response<AdminMappingDTO> createMapping(@RequestBody CreateMappingRequest request) {
        // 检查映射是否已存在
        Mapping existingMapping = mappingRepository.findBySourcePlatformAndPlatformId(
                request.sourcePlatform(),
                request.platformId()
        );

        if (existingMapping != null) {
            throw new ResourceAlreadyExistsException("Mapping", request.sourcePlatform().name() + " - " + request.platformId());
        }

        // 获取对应的 FetchService
        AbstractAnimeFetchService fetchServiceImpl = fetchService.getFetchService(request.sourcePlatform());

        // 从平台获取数据并创建映射
        Mapping mapping = fetchServiceImpl.fetchAndSaveMapping(request.platformId());
        AdminMappingDTO mappingDTO = adminAnimeMapper.toMappingDto(mapping);

        return Response.ok(mappingDTO);
    }
}
