<script setup lang="ts">
import { Filter } from "@element-plus/icons-vue";
import { REVIEW_STATUS_OPTIONS, SEASON_OPTIONS } from "@pjyk-web/shared/constants/ui-options.js";

import type { ReviewStatus } from "@/types/adminAnime";
import type { Season } from "@/types/anime";

const selectedReviewStatus = defineModel<ReviewStatus | null>("selectedReviewStatus", {
  required: true,
});
const selectedYear = defineModel<number | null>("selectedYear", { required: true });
const selectedSeason = defineModel<Season | null>("selectedSeason", { required: true });
</script>

<template>
  <div class="mb-3 flex flex-wrap items-center gap-2">
    <el-icon><Filter /></el-icon>
    <span class="text-sm text-gray-600">筛选：</span>

    <el-select
      v-model="selectedReviewStatus"
      placeholder="审核状态"
      size="small"
      style="width: 110px"
    >
      <el-option
        v-for="option in REVIEW_STATUS_OPTIONS"
        :key="option.label"
        :label="option.label"
        :value="option.value"
      />
    </el-select>

    <el-date-picker
      v-model="selectedYear"
      type="year"
      placeholder="年份"
      size="small"
      style="width: 100px"
      value-format="YYYY"
      clearable
    />

    <el-select
      v-model="selectedSeason"
      placeholder="季度"
      size="small"
      style="width: 100px"
      clearable
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
