<script setup lang="ts">
import { ref, computed } from "vue";
import { useRoute, useRouter, type LocationQueryValue } from "vue-router";
import AnimeFilter from "@/components/public/AnimeFilter.vue";
import AnimeList from "@/components/public/AnimeList.vue";
import type { AnimeListParams } from "@/api/public/anime";
import type { AnimeFilterParams } from "@/types/filter";
import type { PageInfo } from "@/types/page";
import { filtersToQuery, queryToFilters } from "@/utils/filterUtils";
import { SEASON_NAME_MAP, SORT_BY_NAME_MAP } from "@/constants/ui-options";
import { useHead } from "@unhead/vue";

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
  pageSize: 30,
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

// 动态生成页面标题和描述
const pageTitle = computed(() => {
  const parts: string[] = [];

  if (animeListParams.value.year) {
    parts.push(`${animeListParams.value.year}年`);
    if (animeListParams.value.season) {
      parts.push(SEASON_NAME_MAP[animeListParams.value.season]);
    }
    parts.push("动漫");
  } else {
    parts.push("全部动漫");
  }

  if (animeListParams.value.sortBy === "POPULARITY") {
    parts.push(" - 按人气排序");
  }

  return `${parts.join("")} - 有希计划`;
});

const pageDescription = computed(() => {
  const parts: string[] = ["浏览"];

  if (animeListParams.value.year && animeListParams.value.season) {
    parts.push(`${animeListParams.value.year}年${SEASON_NAME_MAP[animeListParams.value.season]}`);
  } else if (animeListParams.value.year) {
    parts.push(`${animeListParams.value.year}年`);
  } else if (animeListParams.value.season) {
    parts.push(SEASON_NAME_MAP[animeListParams.value.season]);
  }

  parts.push(`动漫列表，按${SORT_BY_NAME_MAP[animeListParams.value.sortBy || "SCORE"]}排序。`);
  parts.push("Meta Anime 聚合多个数据源，为您提供最全面的动漫信息和评分。");

  return parts.join("");
});

// 动态生成页面 URL
const pageUrl = computed(() => {
  const url = new URL("https://www.yukiani.com/anime/list");
  if (animeListParams.value.year)
    url.searchParams.set("year", animeListParams.value.year.toString());
  if (animeListParams.value.season) url.searchParams.set("season", animeListParams.value.season);
  if (animeListParams.value.sortBy !== "SCORE")
    url.searchParams.set("sortBy", animeListParams.value.sortBy || "SCORE");
  return url.toString();
});

// 动态 SEO 配置
useHead({
  title: pageTitle,
  meta: [
    {
      name: "description",
      content: pageDescription,
    },
    {
      name: "keywords",
      content: computed(() => {
        const keywords = ["动漫列表", "anime", "番剧", "动画评分"];
        if (animeListParams.value.year) keywords.push(`${animeListParams.value.year}年动漫`);
        if (animeListParams.value.season)
          keywords.push(`${SEASON_NAME_MAP[animeListParams.value.season]}新番`);
        return keywords.join(",");
      }),
    },
    // Open Graph
    {
      property: "og:title",
      content: pageTitle,
    },
    {
      property: "og:description",
      content: pageDescription,
    },
    {
      property: "og:url",
      content: pageUrl,
    },
    // Twitter Card
    {
      name: "twitter:title",
      content: pageTitle,
    },
    {
      name: "twitter:description",
      content: pageDescription,
    },
  ],
  link: [
    {
      rel: "canonical",
      href: pageUrl,
    },
  ],
});
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
