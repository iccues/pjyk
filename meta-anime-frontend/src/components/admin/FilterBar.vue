<script setup lang="ts">
import { Filter } from "@element-plus/icons-vue";
import type { ReviewStatus } from "@/types/adminAnime";
import type { Season } from "@/types/anime";

interface Props {
  selectedReviewStatus?: ReviewStatus;
  selectedYear?: number;
  selectedSeason?: Season;
  reviewStatusOptions: { label: string; value: ReviewStatus | undefined }[];
  yearOptions: { label: string; value: number | undefined }[];
  seasonOptions: { label: string; value: Season | undefined }[];
}

defineProps<Props>();

const emit = defineEmits<{
  "update:selectedReviewStatus": [value: ReviewStatus | undefined];
  "update:selectedYear": [value: number | undefined];
  "update:selectedSeason": [value: Season | undefined];
  change: [];
}>();
</script>

<template>
  <div class="mb-3 flex flex-wrap items-center gap-2">
    <el-icon><Filter /></el-icon>
    <span class="text-sm text-gray-600">筛选：</span>

    <el-select
      :model-value="selectedReviewStatus"
      placeholder="审核状态"
      size="small"
      style="width: 110px"
      @update:model-value="
        emit('update:selectedReviewStatus', $event);
        emit('change');
      "
    >
      <el-option
        v-for="option in reviewStatusOptions"
        :key="option.label"
        :label="option.label"
        :value="option.value"
      />
    </el-select>

    <el-select
      :model-value="selectedYear"
      placeholder="年份"
      size="small"
      style="width: 100px"
      clearable
      @update:model-value="
        emit('update:selectedYear', $event);
        emit('change');
      "
    >
      <el-option
        v-for="option in yearOptions"
        :key="option.label"
        :label="option.label"
        :value="option.value"
      />
    </el-select>

    <el-select
      :model-value="selectedSeason"
      placeholder="季度"
      size="small"
      style="width: 100px"
      clearable
      @update:model-value="
        emit('update:selectedSeason', $event);
        emit('change');
      "
    >
      <el-option
        v-for="option in seasonOptions"
        :key="option.label"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </div>
</template>
