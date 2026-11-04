package com.yukiani.server.controller;

import com.yukiani.server.common.Response;
import com.yukiani.server.dto.admin.AdminMappingDTO;
import com.yukiani.server.dto.admin.UpdateMappingAnimeRequest;
import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.PlatformKey;
import com.yukiani.server.mapper.AdminAnimeMapper;
import com.yukiani.server.service.MappingManageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提供管理端外部平台 Mapping 的查询与维护 API。
 */
@RestController
@RequestMapping("/api/admin/mappings")
public class AdminMappingController {

    @Resource
    MappingManageService mappingManageService;

    @Resource
    AdminAnimeMapper adminAnimeMapper;

    /**
     * 查询尚未关联动画的平台 Mapping。
     *
     * @return 包含未关联 Mapping 列表的成功响应；无结果时列表为空
     */
    @GetMapping("/unlinked")
    public Response<List<AdminMappingDTO>> getUnlinkedMappings() {
        List<Mapping> mappingList = mappingManageService.getUnmappedMappingList();
        List<AdminMappingDTO> mappingDTOList = adminAnimeMapper.toMappingDtoList(mappingList);
        return Response.ok(mappingDTOList);
    }

    /**
     * 更新 Mapping 的动画关联；request.animeId 为空时解除关联。
     *
     * @param request 包含 mappingId 和 animeId；animeId 为 {@code null} 时解除关联
     * @return 包含更新后 Mapping 的成功响应
     */
    @PutMapping("/anime")
    public Response<AdminMappingDTO> updateMappingAnime(@RequestBody UpdateMappingAnimeRequest request) {
        Mapping savedMapping = mappingManageService.updateMappingAnime(request.mappingId(), request.animeId());
        AdminMappingDTO mappingDTO = adminAnimeMapper.toMappingDto(savedMapping);
        return Response.ok(mappingDTO);
    }

    /**
     * 删除指定平台 Mapping 并更新原动画的聚合指标。
     *
     * @param mappingId 待删除 Mapping 的 mappingId
     * @return 不包含响应数据的成功响应
     */
    @DeleteMapping("/{mappingId}")
    public Response<Void> deleteMapping(@PathVariable Long mappingId) {
        mappingManageService.deleteMapping(mappingId);
        return Response.ok(null);
    }

    /**
     * 从指定平台即时抓取并创建 Mapping。
     *
     * @param platformKey 待创建 Mapping 的平台条目标识
     * @return 包含已创建 Mapping 的成功响应
     */
    @PostMapping
    public Response<AdminMappingDTO> createMapping(@RequestBody PlatformKey platformKey) {
        Mapping mapping = mappingManageService.createMapping(platformKey);
        AdminMappingDTO mappingDTO = adminAnimeMapper.toMappingDto(mapping);
        return Response.ok(mappingDTO);
    }
}
