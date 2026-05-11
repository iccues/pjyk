import { ElMessage } from "element-plus";
import { defineStore } from "pinia";
import { ref } from "vue";

import { updateMappingAnime } from "@/api/mapping.ts";
import type { AdminAnime, AdminMapping } from "@/types/adminAnime.ts";

export const useAdminListPageStore = defineStore("adminListPage", () => {
  const animeList = ref<AdminAnime[]>([]);
  const mappingList = ref<AdminMapping[]>([]);

  // 添加或更新动画到列表
  const upsertAnime = (anime: AdminAnime) => {
    const idx = animeList.value.findIndex((a) => a.animeId === anime.animeId);
    if (idx !== -1) {
      animeList.value[idx] = anime;
    } else {
      animeList.value.unshift(anime);
    }
  };

  // 从动画列表中移除动画
  const removeAnime = (animeId: number) => {
    const idx = animeList.value.findIndex((a) => a.animeId === animeId);
    if (idx !== -1) animeList.value.splice(idx, 1);
  };

  // 添加映射到指定动画
  const addMappingToAnime = (animeId: number, mapping: AdminMapping) => {
    const anime = animeList.value.find((a) => a.animeId === animeId);
    if (anime && !anime.mappings.some((m) => m.mappingId === mapping.mappingId)) {
      anime.mappings.push(mapping);
    }
  };

  // 从动画中移除映射
  const removeMappingFromAnime = (animeId: number, mappingId: number) => {
    const anime = animeList.value.find((a) => a.animeId === animeId);
    if (anime) {
      const idx = anime.mappings.findIndex((m) => m.mappingId === mappingId);
      if (idx !== -1) anime.mappings.splice(idx, 1);
    }
  };

  // 添加映射到未关联列表
  const addMappingToUnmapped = (mapping: AdminMapping) => {
    mapping.animeId = null;
    if (!mappingList.value.some((m) => m.mappingId === mapping.mappingId)) {
      mappingList.value.push(mapping);
    }
  };

  // 从未关联列表中移除映射
  const removeMappingFromUnmapped = (mappingId: number) => {
    const idx = mappingList.value.findIndex((m) => m.mappingId === mappingId);
    if (idx !== -1) mappingList.value.splice(idx, 1);
  };

  // 应用映射变化
  const applyMappingChange = async (mapping: AdminMapping, newAnimeId: number | null) => {
    const oldAnimeId = mapping.animeId;
    if (oldAnimeId === newAnimeId) return;

    try {
      await updateMappingAnime(mapping.mappingId, newAnimeId);

      // 1. 从原位置移除
      if (oldAnimeId !== null) {
        removeMappingFromAnime(oldAnimeId, mapping.mappingId);
      } else {
        removeMappingFromUnmapped(mapping.mappingId);
      }

      // 2. 更新数据的 animeId
      mapping.animeId = newAnimeId;

      // 3. 添加到新位置
      if (newAnimeId !== null) {
        addMappingToAnime(newAnimeId, mapping);
        ElMessage.success("映射关联成功");
      } else {
        addMappingToUnmapped(mapping);
        ElMessage.success("已解除映射关联");
      }
    } catch (e) {
      ElMessage.error("更新失败: " + (e instanceof Error ? e.message : "未知错误"));
    }
  };

  return {
    animeList,
    mappingList,
    upsertAnime,
    removeAnime,
    addMappingToAnime,
    removeMappingFromAnime,
    addMappingToUnmapped,
    removeMappingFromUnmapped,
    applyMappingChange,
  };
});
