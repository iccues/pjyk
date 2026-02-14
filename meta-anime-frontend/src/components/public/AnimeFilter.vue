<script setup lang="ts">
import { Filter } from "@element-plus/icons-vue";
import { computed } from "vue";
import { SEASON_OPTIONS, SORT_BY_OPTIONS } from "@/constants/ui-options";
import type { AnimeFilterParams } from "@/types/filter";
import { generateYearOptions } from "@/utils/dateUtils";

const props = defineProps<{
  filters: AnimeFilterParams;
}>();

const emit = defineEmits<{
  "update:filters": [filters: AnimeFilterParams];
}>();

// 使用统一的常量和工具函数
const seasonOptions = SEASON_OPTIONS;
const sortByOptions = SORT_BY_OPTIONS;
const yearOptions = computed(() => generateYearOptions(10));

// 处理筛选变化
const handleYearChange = (value: number | undefined) => {
  emit("update:filters", {
    ...props.filters,
    year: value,
    // 清空季度如果年份被清除
    season: value === undefined ? undefined : props.filters.season,
  });
};

const handleSeasonChange = (value: string | undefined) => {
  emit("update:filters", {
    ...props.filters,
    season: value as any,
  });
};

const handleSortByChange = (value: string) => {
  emit("update:filters", {
    ...props.filters,
    sortBy: value as any,
  });
};
</script>

<template>
  <div class="mb-6 grid grid-cols-[repeat(auto-fill,12.5rem)] gap-5 justify-center">
    <div class="flex items-center gap-5 col-span-full">
      <el-icon size="20"><Filter /></el-icon>

      <el-select
        :model-value="filters.year"
        placeholder="年份"
        size="medium"
        class="!w-40"
        clearable
        @change="handleYearChange"
      >
        <el-option
          v-for="option in yearOptions"
          :key="option.label"
          :label="option.label"
          :value="option.value"
        />
      </el-select>

      <el-select
        :model-value="filters.season"
        placeholder="季度"
        size="medium"
        class="!w-40"
        clearable
        @change="handleSeasonChange"
        :disabled="filters.year === undefined"
      >
        <el-option
          v-for="option in seasonOptions"
          :key="option.label"
          :label="option.label"
          :value="option.value"
        />
      </el-select>

      <el-select
        :model-value="filters.sortBy"
        placeholder="排序方式"
        size="medium"
        class="!w-40"
        @change="handleSortByChange"
      >
        <el-option
          v-for="option in sortByOptions"
          :key="option.label"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </div>
  </div>
</template>
