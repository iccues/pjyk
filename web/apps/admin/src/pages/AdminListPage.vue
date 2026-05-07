<script setup lang="ts">
import { ElMessage } from "element-plus";
import { ref } from "vue";

import { updateMappingAnime } from "@/api/mapping";
import AnimeListSection from "@/components/AnimeListSection.vue";
import MappingListSection from "@/components/MappingListSection.vue";
import type { AdminAnime, AdminMapping } from "@/types/adminAnime";

const animeList = ref<AdminAnime[]>([]);
const mappingList = ref<AdminMapping[]>([]);

// --- 1. 数据操作辅助函数 ---

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

// --- 2. 核心业务逻辑 ---

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
</script>

<template>
  <div class="mx-auto flex h-full max-w-[1800px] flex-col overflow-hidden p-5 pb-0">
    <el-row :gutter="24" class="flex-1 overflow-hidden">
      <!-- 左列：动画列表 -->
      <el-col :xs="24" :lg="12" class="h-full">
        <AnimeListSection
          v-model:animeList="animeList"
          @mapping-add="(evt, animeId) => applyMappingChange(evt.data, animeId)"
          @add-mapping-to-unmapped="addMappingToUnmapped"
        />
      </el-col>

      <!-- 右列：未关联映射列表 -->
      <el-col :xs="24" :lg="12" class="h-full">
        <MappingListSection
          v-model:mappingList="mappingList"
          @mapping-add="applyMappingChange($event.data, null)"
        />
      </el-col>
    </el-row>
  </div>
</template>
