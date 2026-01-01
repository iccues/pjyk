<script setup lang="ts">
import { Filter } from "@element-plus/icons-vue";
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import AnimeList from "@/components/public/AnimeList.vue";
import { SEASON_OPTIONS } from "@/constants/ui-options";
import type { Season } from "@/types/anime";
import type { PageInfo } from "@/types/page";
import { generateYearOptions } from "@/utils/dateUtils";

const router = useRouter();
const route = useRoute();

// 从 URL Query 初始化状态的辅助函数
const getInitialValue = <T>(
  key: string,
  parser: (value: string) => T | undefined,
  defaultValue: T,
): T => {
  const value = route.query[key];
  if (value && typeof value === "string") {
    const parsed = parser(value);
    if (parsed !== undefined) {
      return parsed;
    }
  }
  return defaultValue;
};

const parseYear = (value: string): number | undefined => {
  const num = parseInt(value);
  return isNaN(num) ? undefined : num;
};

const parseSeason = (value: string): Season | undefined => {
  const validSeasons: Season[] = ["WINTER", "SPRING", "SUMMER", "FALL"];
  return validSeasons.includes(value as Season) ? (value as Season) : undefined;
};

const parsePage = (value: string): number => {
  const num = parseInt(value);
  return isNaN(num) || num < 0 ? 0 : num;
};

// 筛选器状态 - 直接从 URL 初始化
const selectedYear = ref<number | undefined>(
  getInitialValue("year", parseYear, undefined),
);
const selectedSeason = ref<Season | undefined>(
  getInitialValue("season", parseSeason, undefined),
);
const currentPage = ref(getInitialValue("page", parsePage, 0));
const pageSize = ref(60);
const pageInfo = ref<PageInfo | null>(null);

// 更新 URL Query
const updateQuery = () => {
  const query: Record<string, string> = {};

  if (selectedYear.value !== undefined) {
    query.year = selectedYear.value.toString();
  }

  if (selectedSeason.value !== undefined) {
    query.season = selectedSeason.value;
  }

  if (currentPage.value > 0) {
    query.page = currentPage.value.toString();
  }

  router.push({ query });
};

// 使用统一的常量和工具函数
const seasonOptions = SEASON_OPTIONS;
const yearOptions = computed(() => generateYearOptions(10));

// 处理筛选变化
const handleFilterChange = () => {
  currentPage.value = 0; // 重置到第一页
  updateQuery();
};

// 处理分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page - 1; // Element Plus 的页码从1开始，API从0开始
  updateQuery();
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: "smooth" });
};
</script>

<template>
  <div class="p-5 max-w-[1400px] mx-auto">
    <!-- 筛选器 -->
    <div class="mb-6 grid grid-cols-[repeat(auto-fill,12.5rem)] gap-5 justify-center">
      <div class="flex items-center gap-5 col-span-full">
        <el-icon size="20"><Filter /></el-icon>

        <el-select
          v-model="selectedYear"
          placeholder="年份"
          size="medium"
          class="!w-40"
          clearable
          @change="handleFilterChange"
        >
          <el-option
            v-for="option in yearOptions"
            :key="option.label"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-select
          v-model="selectedSeason"
          placeholder="季度"
          size="medium"
          class="!w-40"
          clearable
          @change="handleFilterChange"
        >
          <el-option
            v-for="option in seasonOptions"
            :key="option.label"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>
    </div>

    <AnimeList
      :year="selectedYear"
      :season="selectedSeason"
      :page="currentPage"
      :page-size="pageSize"
      v-on:update:page-info="pageInfo_ => {
        pageInfo = pageInfo_;
      }"
    />

    <div v-if="pageInfo" class="flex justify-center mt-8">
      <el-pagination
        :current-page="currentPage + 1"
        :page-size="pageSize"
        :total="pageInfo.totalElements"
        :page-count="pageInfo.totalPages"
        layout="prev, pager, next, total"
        @current-change="handlePageChange"
      />
    </div>

  </div>
</template>

<style scoped>
:deep(.el-pagination) {
  justify-content: center;
}
</style>
