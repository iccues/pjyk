package com.yukiani.controller;

import com.yukiani.common.Response;
import com.yukiani.dto.admin.AdminMappingDTO;
import com.yukiani.dto.admin.CreateMappingRequest;
import com.yukiani.dto.admin.UpdateMappingAnimeRequest;
import com.yukiani.entity.Mapping;
import com.yukiani.mapper.AdminAnimeMapper;
import com.yukiani.service.MappingManageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin")
public class AdminMappingController {

    @Resource
    MappingManageService mappingManageService;

    @Resource
    AdminAnimeMapper adminAnimeMapper;

    @ResponseBody
    @GetMapping("/get_unmapped_mapping_list")
    public Response<List<AdminMappingDTO>> getUnmappedMappingList() {
        List<Mapping> mappingList = mappingManageService.getUnmappedMappingList();
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
    public Response<AdminMappingDTO> updateMappingAnime(@RequestBody UpdateMappingAnimeRequest request) {
        Mapping savedMapping = mappingManageService.updateMappingAnime(request.mappingId(), request.animeId());
        AdminMappingDTO mappingDTO = adminAnimeMapper.toMappingDto(savedMapping);
        return Response.ok(mappingDTO);
    }

    @ResponseBody
    @DeleteMapping("/delete_mapping/{mappingId}")
    public Response<Void> deleteMapping(@PathVariable Long mappingId) {
        mappingManageService.deleteMapping(mappingId);
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
        Mapping mapping = mappingManageService.createMapping(request.sourcePlatform(), request.platformId());
        AdminMappingDTO mappingDTO = adminAnimeMapper.toMappingDto(mapping);
        return Response.ok(mappingDTO);
    }
}
