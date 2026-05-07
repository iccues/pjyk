<script setup lang="ts">
import { Plus, Refresh } from "@element-plus/icons-vue";
import { useQuery } from "@tanstack/vue-query";
import { ElMessage, ElMessageBox } from "element-plus";
import { storeToRefs } from "pinia";
import { VList } from "virtua/vue";
import { computed, ref, toRaw, watch } from "vue";

import { deleteAnime, getAnimeList, updateAnime } from "@/api/anime";
import AdminAnimeItem from "@/components/AdminAnimeItem.vue";
import AnimeFormDialog from "@/components/AnimeFormDialog.vue";
import FilterBar from "@/components/FilterBar.vue";
import { useAdminListPageStore } from "@/stores/adminListPageStore.ts";
import type { AdminAnime, ReviewStatus } from "@/types/adminAnime";
import type { Season } from "@/types/anime";

// --- 1. Store 状态与核心逻辑 ---

const { animeList } = storeToRefs(useAdminListPageStore());

const { applyMappingChange, addMappingToUnmapped, removeAnime } = useAdminListPageStore();

const dialogVisible = ref(false);
const editingAnime = ref<AdminAnime | null>(null);

const selectedReviewStatus = ref<ReviewStatus | null>("PENDING");
const selectedYear = ref<number | null>(null);
const selectedSeason = ref<Season | null>(null);
const searchKeyword = ref("");

// --- 2. 数据加载与筛选 (TanStack Query) ---

const { isFetching, isError, error, data, refetch } = useQuery({
  queryKey: ["admin-anime-list", selectedReviewStatus, selectedYear, selectedSeason],
  queryFn: ({ signal }) =>
    getAnimeList(selectedReviewStatus.value, selectedYear.value, selectedSeason.value, signal),
});

watch(
  data,
  (newVal) => {
    if (newVal) animeList.value = structuredClone(toRaw(newVal));
  },
  { immediate: true },
);

const titleMatches = (
  title:
    | { titleCn?: string; titleNative?: string; titleRomaji?: string; titleEn?: string }
    | undefined,
  keyword: string,
) => {
  if (!title) return false;
  return (
    title.titleCn?.toLowerCase().includes(keyword) ||
    title.titleNative?.toLowerCase().includes(keyword) ||
    title.titleRomaji?.toLowerCase().includes(keyword) ||
    title.titleEn?.toLowerCase().includes(keyword)
  );
};

const isAnimeMatch = (anime: AdminAnime, keyword: string) => {
  if (String(anime.animeId) === keyword) return true;
  if (titleMatches(anime.title, keyword)) return true;
  return anime.mappings.some(
    (m) =>
      String(m.mappingId) === keyword ||
      m.platformId === keyword ||
      titleMatches(m.mappingInfo?.title, keyword),
  );
};

const filteredAnimeList = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase();
  if (!keyword) return animeList.value;
  return animeList.value.filter((anime) => isAnimeMatch(anime, keyword));
});

// --- 3. 组件本地 UI 操作 ---

const handleEditAnime = (anime: AdminAnime | null) => {
  editingAnime.value = anime;
  dialogVisible.value = true;
};

const handleDeleteAnime = async (animeId: number) => {
  const anime = animeList.value.find((a) => a.animeId === animeId);
  if (!anime) return;

  try {
    const mappingCount = anime.mappings.length;
    const confirmMessage =
      mappingCount > 0
        ? `确定要删除动画《${anime.title.titleCn || anime.title.titleNative}》吗？\n该动画包含 ${mappingCount} 个映射，将先解除关联后再删除动画。\n此操作不可恢复。`
        : `确定要删除动画《${anime.title.titleCn || anime.title.titleNative}》吗？此操作不可恢复。`;

    await ElMessageBox.confirm(confirmMessage, "确认删除", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
      dangerouslyUseHTMLString: false,
    });

    const mappingsToMove = [...anime.mappings];
    await deleteAnime(animeId);

    mappingsToMove.forEach((mapping) => {
      addMappingToUnmapped(mapping);
    });

    removeAnime(animeId);
    ElMessage.success("删除成功");
  } catch (e) {
    if (e === "cancel") return;
    ElMessage.error("删除失败: " + (e instanceof Error ? e.message : "未知错误"));
  }
};

const handleUpdateReviewStatus = async (animeId: number, reviewStatus: ReviewStatus) => {
  try {
    const anime = animeList.value.find((a) => a.animeId === animeId);
    if (!anime) return;

    const updated = await updateAnime({ animeId, reviewStatus });
    const idx = animeList.value.findIndex((a) => a.animeId === animeId);

    if (idx !== -1) {
      if (selectedReviewStatus.value && reviewStatus !== selectedReviewStatus.value) {
        animeList.value.splice(idx, 1);
        ElMessage.success("审核状态已更新，该动画已从当前筛选列表中移除");
      } else {
        animeList.value[idx] = updated;
        ElMessage.success("审核状态更新成功");
      }
    }
  } catch (e) {
    ElMessage.error("更新失败: " + (e instanceof Error ? e.message : "未知错误"));
  }
};
</script>

<template>
  <div class="flex h-full flex-col">
    <div class="mb-4 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <h2 class="m-0 text-xl font-semibold text-gray-800">动画列表</h2>
        <el-tag type="info">{{ filteredAnimeList.length }} / {{ animeList.length }}</el-tag>
      </div>
      <div class="flex items-center gap-2">
        <el-button :icon="Refresh" circle size="small" @click="refetch" />
        <el-button type="primary" size="small" :icon="Plus" @click="handleEditAnime(null)">
          新建动画
        </el-button>
      </div>
    </div>

    <el-input v-model="searchKeyword" placeholder="搜索标题 / ID..." class="mb-3" />

    <FilterBar
      v-model:selectedReviewStatus="selectedReviewStatus"
      v-model:selectedYear="selectedYear"
      v-model:selectedSeason="selectedSeason"
    />

    <el-alert
      v-if="isError"
      v-loading="isFetching"
      :title="error?.message || '加载失败'"
      type="error"
      center
      show-icon
      :closable="false"
      class="mb-4"
    />

    <VList
      v-else
      v-loading="isFetching"
      :data="filteredAnimeList"
      class="flex-1 overflow-auto pr-2"
    >
      <template #default="{ item: anime }">
        <div :key="anime.animeId" class="mb-3">
          <AdminAnimeItem
            :anime="anime"
            @mapping-add="applyMappingChange($event.data, anime.animeId)"
            @delete-anime="handleDeleteAnime"
            @edit-anime="handleEditAnime"
            @update-review-status="handleUpdateReviewStatus"
          />
        </div>
      </template>
    </VList>

    <!-- 动画表单对话框 -->
    <AnimeFormDialog v-model:visible="dialogVisible" :anime="editingAnime" />
  </div>
</template>
