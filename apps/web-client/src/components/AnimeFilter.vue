<script setup lang="ts">
import { Filter } from "@element-plus/icons-vue";
import { computed } from "vue";
import { SEASON_OPTIONS, SORT_BY_OPTIONS } from "@/constants/ui-options";
import type { AnimeListParams } from "@/api/anime";
import { generateYearOptions } from "@/utils/dateUtils";

const props = defineProps<{
  modelValue: AnimeListParams;
}>();

const emit = defineEmits<{
  "update:modelValue": [filters: AnimeListParams];
}>();

// 使用统一的常量和工具函数
const seasonOptions = SEASON_OPTIONS;
const sortByOptions = SORT_BY_OPTIONS;
const yearOptions = computed(() => generateYearOptions(10));

// 处理筛选变化
const handleYearChange = (value: number | undefined) => {
  handleFilterChange({
    ...props.modelValue,
    year: value,
    // 清空季度如果年份被清除
    season: value === undefined ? undefined : props.modelValue.season,
  });
};

const handleSeasonChange = (value: string | undefined) => {
  handleFilterChange({
    ...props.modelValue,
    season: value as any,
  });
};

const handleSortByChange = (value: string) => {
  handleFilterChange({
    ...props.modelValue,
    sortBy: value as any,
  });
};

const handleFilterChange = (fliter: AnimeListParams) => {
  emit("update:modelValue", {
    ...fliter,
    page: 0,
  });
};
</script>

<template>
  <div class="mb-6 grid grid-cols-[repeat(auto-fill,12.5rem)] gap-5 justify-center">
    <div class="flex items-center gap-5 col-span-full">
      <el-icon size="20"><Filter /></el-icon>

      <el-select
        :model-value="modelValue.year"
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
        :model-value="modelValue.season"
        placeholder="季度"
        size="medium"
        class="!w-40"
        clearable
        @change="handleSeasonChange"
        :disabled="modelValue.year === undefined"
      >
        <el-option
          v-for="option in seasonOptions"
          :key="option.label"
          :label="option.label"
          :value="option.value"
        />
      </el-select>

      <el-select
        :model-value="modelValue.sortBy"
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
