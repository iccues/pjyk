<script setup lang="ts">
import { Plus, Refresh } from "@element-plus/icons-vue";
import { VList } from "virtua/vue";
import { computed, ref } from "vue";

import AdminAnimeItem from "@/components/AdminAnimeItem.vue";
import AnimeFormDialog from "@/components/AnimeFormDialog.vue";
import FilterBar from "@/components/FilterBar.vue";
import type { AdminAnime, ReviewStatus } from "@/types/adminAnime";
import type { Season } from "@/types/anime";

interface Props {
  animeList: AdminAnime[];
  selectedReviewStatus?: ReviewStatus;
  selectedYear?: number;
  selectedSeason?: Season;
  reviewStatusOptions: { label: string; value: ReviewStatus | undefined }[];
  yearOptions: { label: string; value: number | undefined }[];
  seasonOptions: { label: string; value: Season | undefined }[];
  dialogVisible: boolean;
  editingAnime?: AdminAnime;
  loading?: boolean;
  error?: string | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  "update:selectedReviewStatus": [value: ReviewStatus | undefined];
  "update:selectedYear": [value: number | undefined];
  "update:selectedSeason": [value: Season | undefined];
  "filter-change": [];
  "create-anime": [];
  "edit-anime": [anime: AdminAnime];
  "delete-anime": [animeId: number];
  "update-review-status": [animeId: number, reviewStatus: ReviewStatus];
  "mapping-change": [evt: any, animeId: number];
  "close-dialog": [];
  "submit-form": [formData: any];
  refresh: [];
}>();

const searchKeyword = ref("");

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

const filteredAnimeList = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase();
  if (!keyword) return props.animeList;
  return props.animeList.filter((anime) => {
    if (String(anime.animeId) === keyword) return true;
    if (titleMatches(anime.title, keyword)) return true;
    return anime.mappings.some(
      (m) =>
        String(m.mappingId) === keyword ||
        m.platformId.toLowerCase().includes(keyword) ||
        titleMatches(m.mappingInfo?.title, keyword),
    );
  });
});
</script>

<template>
  <div class="flex h-full flex-col" v-loading="loading">
    <div class="mb-4 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <h2 class="m-0 text-xl font-semibold text-gray-800">动画列表</h2>
        <el-tag type="info">{{ filteredAnimeList.length }} / {{ animeList.length }}</el-tag>
      </div>
      <div class="flex items-center gap-2">
        <el-button :icon="Refresh" circle size="small" @click="emit('refresh')" />
        <el-button type="primary" size="small" :icon="Plus" @click="emit('create-anime')">
          新建动画
        </el-button>
      </div>
    </div>

    <el-input v-model="searchKeyword" placeholder="搜索标题 / ID..." class="mb-3" />

    <FilterBar
      :selected-review-status="selectedReviewStatus"
      :selected-year="selectedYear"
      :selected-season="selectedSeason"
      :review-status-options="reviewStatusOptions"
      :year-options="yearOptions"
      :season-options="seasonOptions"
      @update:selected-review-status="emit('update:selectedReviewStatus', $event)"
      @update:selected-year="emit('update:selectedYear', $event)"
      @update:selected-season="emit('update:selectedSeason', $event)"
      @change="emit('filter-change')"
    />

    <el-alert
      v-if="error"
      :title="error"
      type="error"
      center
      show-icon
      :closable="false"
      class="mb-4"
    />

    <VList v-else :data="filteredAnimeList" class="flex-1 overflow-auto pr-2">
      <template #default="{ item: anime }">
        <div :key="anime.animeId" class="mb-3">
          <AdminAnimeItem
            :anime="anime"
            @mapping-change="(evt) => emit('mapping-change', evt, anime.animeId)"
            @delete-anime="emit('delete-anime', $event)"
            @edit-anime="emit('edit-anime', $event)"
            @update-review-status="
              (animeId, reviewStatus) => emit('update-review-status', animeId, reviewStatus)
            "
          />
        </div>
      </template>
    </VList>

    <!-- 动画表单对话框 -->
    <AnimeFormDialog
      :visible="dialogVisible"
      :anime="editingAnime"
      @close="emit('close-dialog')"
      @submit="emit('submit-form', $event)"
    />
  </div>
</template>
