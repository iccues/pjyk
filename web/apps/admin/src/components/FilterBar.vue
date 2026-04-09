<script setup lang="ts">
import { Filter } from "@element-plus/icons-vue";
import { REVIEW_STATUS_OPTIONS, SEASON_OPTIONS } from "@pjyk-web/shared/constants/ui-options.js";

import type { ReviewStatus } from "@/types/adminAnime";
import type { Season } from "@/types/anime";

interface Props {
  selectedReviewStatus?: ReviewStatus;
  selectedYear?: number;
  selectedSeason?: Season;
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
        v-for="option in REVIEW_STATUS_OPTIONS"
        :key="option.label"
        :label="option.label"
        :value="option.value"
      />
    </el-select>

    <el-date-picker
      :model-value="selectedYear ? String(selectedYear) : null"
      type="year"
      placeholder="年份"
      size="small"
      style="width: 100px"
      value-format="YYYY"
      clearable
      @update:model-value="
        emit('update:selectedYear', $event ? Number($event) : undefined);
        emit('change');
      "
    />

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
        v-for="option in SEASON_OPTIONS"
        :key="option.label"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </div>
</template>
