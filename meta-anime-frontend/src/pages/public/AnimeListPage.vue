<script setup lang="ts">
import { ref } from "vue";
import { useRoute, useRouter, type LocationQueryValue } from "vue-router";
import AnimeFilter from "@/components/public/AnimeFilter.vue";
import AnimeList from "@/components/public/AnimeList.vue";
import type { AnimeListParams } from "@/api/public/anime";
import type { AnimeFilterParams } from "@/types/filter";
import type { PageInfo } from "@/types/page";
import { filtersToQuery, queryToFilters } from "@/utils/filterUtils";

const router = useRouter();
const route = useRoute();

// 从 URL Query 初始化当前页码
const parsePage = (page: LocationQueryValue | LocationQueryValue[] | undefined): number => {
  if (page && typeof page === "string") {
    const num = parseInt(page);
    return isNaN(num) || num < 0 ? 0 : num;
  }
  return 0;
};

// 单一数据源：所有列表参数
const animeListParams = ref<AnimeListParams>({
  ...queryToFilters(route.query),
  page: parsePage(route.query.page),
  pageSize: 60,
});

const pageInfo = ref<PageInfo | null>(null);

// 更新 URL Query
const updateQuery = () => {
  const query: Record<string, string> = {
    ...filtersToQuery(animeListParams.value),
  };

  if (animeListParams.value.page && animeListParams.value.page > 0) {
    query.page = animeListParams.value.page.toString();
  }

  router.push({ query });
};

// 处理筛选变化
const handleFilterChange = (newFilters: AnimeFilterParams) => {
  // 更新筛选参数并重置页码
  animeListParams.value = {
    ...animeListParams.value,
    ...newFilters,
    page: 0,
  };
  updateQuery();
};

// 处理分页变化
const handlePageChange = (page: number) => {
  animeListParams.value.page = page - 1; // Element Plus 的页码从1开始，API从0开始
  updateQuery();
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: "smooth" });
};
</script>

<template>
  <div class="p-5 max-w-[1400px] mx-auto">
    <!-- 筛选器 -->
    <AnimeFilter :filters="animeListParams" @update:filters="handleFilterChange" />

    <AnimeList
      v-bind="animeListParams"
      v-on:update:page-info="
        (pageInfo_) => {
          pageInfo = pageInfo_;
        }
      "
    />

    <div v-if="pageInfo" class="flex justify-center mt-8">
      <el-pagination
        :current-page="(animeListParams.page || 0) + 1"
        :page-size="animeListParams.pageSize"
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
