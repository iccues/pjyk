<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import type { AnimeCreateRequest, AnimeUpdateRequest } from "@/api/admin";
import AnimeListSection from "@/components/admin/AnimeListSection.vue";
import MappingListSection from "@/components/admin/MappingListSection.vue";
import { useAnimeList } from "@/composables/admin/useAnimeList";
import { useMappingList } from "@/composables/admin/useMappingList";
import { REVIEW_STATUS_OPTIONS, SEASON_OPTIONS } from "@/constants/ui-options";
import type { AdminMapping, ReviewStatus } from "@/types/adminAnime";
import type { Season } from "@/types/anime";
import type { DraggableChangeEvent } from "@/types/draggable";
import { generateYearOptions } from "@/utils/dateUtils";
import "element-plus/dist/index.css";

// 筛选器状态
const selectedReviewStatus = ref<ReviewStatus | undefined>("PENDING");
const selectedYear = ref<number | undefined>(undefined);
const selectedSeason = ref<Season | undefined>(undefined);

// 筛选器选项
const reviewStatusOptions = REVIEW_STATUS_OPTIONS;
const seasonOptions = SEASON_OPTIONS;
const yearOptions = computed(() => generateYearOptions(10));

const {
  animeList,
  loading,
  error,
  dialogVisible,
  editingAnime,
  loadAnimeList,
  handleDeleteAnime,
  handleCreateAnime,
  handleEditAnime,
  handleCloseDialog,
  handleSubmitForm,
  handleUpdateReviewStatus,
  addMappingToAnime,
  removeMappingFromAnime,
} = useAnimeList();

const {
  mappingList,
  mappingDialogVisible,
  loadMappingList,
  handleMappingToAnime,
  handleMappingToUnmapped,
  handleCreateMapping,
  handleCloseMappingDialog,
  handleSubmitMapping,
  handleDeleteMapping,
  addMappingToUnmapped,
} = useMappingList();

// 处理筛选变化
const handleFilterChange = () => {
  loadAnimeList(selectedReviewStatus.value, selectedYear.value, selectedSeason.value);
};

// 初始化加载数据
onMounted(async () => {
  await Promise.all([
    loadAnimeList(selectedReviewStatus.value, selectedYear.value, selectedSeason.value),
    loadMappingList(),
  ]);
});

// 封装映射操作的回调
const onAnimeRemoveMapping = (animeId: number, mappingId: number) => {
  removeMappingFromAnime(animeId, mappingId);
};

const onAnimeAddMapping = (animeId: number, mapping: AdminMapping) => {
  addMappingToAnime(animeId, mapping);
};

// 删除动画时处理映射移动
const onDeleteAnime = (animeId: number) => {
  handleDeleteAnime(animeId, (mappings) => {
    mappings.forEach((mapping) => {
      addMappingToUnmapped(mapping);
    });
  });
};

// 包装映射拖拽事件
const onMappingToAnime = (evt: DraggableChangeEvent<AdminMapping>, animeId: number) => {
  handleMappingToAnime(evt, animeId, onAnimeRemoveMapping, onAnimeAddMapping);
};

const onMappingToUnmapped = (evt: DraggableChangeEvent<AdminMapping>) => {
  handleMappingToUnmapped(evt, onAnimeRemoveMapping, onAnimeAddMapping);
};

// 包装表单提交
const onSubmitForm = (
  formData: AnimeCreateRequest | (AnimeUpdateRequest & { animeId?: never }),
) => {
  handleSubmitForm(formData, selectedReviewStatus.value);
};

// 包装审核状态更新
const onUpdateReviewStatus = (animeId: number, reviewStatus: ReviewStatus) => {
  handleUpdateReviewStatus(animeId, reviewStatus, selectedReviewStatus.value);
};
</script>

<template>
  <div class="flex flex-col h-full p-5 max-w-[1800px] mx-auto overflow-hidden" v-loading="loading">
    <el-alert v-if="error" :title="error" type="error" center show-icon :closable="false" />

    <el-row v-else :gutter="24" class="flex-1 overflow-hidden">
      <!-- 左列：动画列表 -->
      <el-col :xs="24" :lg="12" class="h-full">
        <AnimeListSection
          :anime-list="animeList"
          :selected-review-status="selectedReviewStatus"
          :selected-year="selectedYear"
          :selected-season="selectedSeason"
          :review-status-options="reviewStatusOptions"
          :year-options="yearOptions"
          :season-options="seasonOptions"
          :dialog-visible="dialogVisible"
          :editing-anime="editingAnime"
          @update:selected-review-status="selectedReviewStatus = $event"
          @update:selected-year="selectedYear = $event"
          @update:selected-season="selectedSeason = $event"
          @filter-change="handleFilterChange"
          @create-anime="handleCreateAnime"
          @edit-anime="handleEditAnime"
          @delete-anime="onDeleteAnime"
          @update-review-status="onUpdateReviewStatus"
          @mapping-change="onMappingToAnime"
          @close-dialog="handleCloseDialog"
          @submit-form="onSubmitForm"
        />
      </el-col>

      <!-- 右列：未关联映射列表 -->
      <el-col :xs="24" :lg="12" class="h-full">
        <MappingListSection
          :mapping-list="mappingList"
          :mapping-dialog-visible="mappingDialogVisible"
          @create-mapping="handleCreateMapping"
          @delete-mapping="handleDeleteMapping"
          @mapping-change="onMappingToUnmapped"
          @close-dialog="handleCloseMappingDialog"
          @submit-mapping="handleSubmitMapping"
        />
      </el-col>
    </el-row>
  </div>
</template>
