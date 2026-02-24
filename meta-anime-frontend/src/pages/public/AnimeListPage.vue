<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import AnimeFilter from "@/components/public/AnimeFilter.vue";
import AnimeList from "@/components/public/AnimeList.vue";
import { getAnimeList, type AnimeListParams } from "@/api/public/anime";
import type { Page } from "@/types/page";
import { filtersToQuery, queryToFilters } from "@/utils/queryUtils";
import { SEASON_NAME_MAP, SORT_BY_NAME_MAP } from "@/constants/ui-options";
import { useHead } from "@unhead/vue";
import type { Anime } from "@/types/anime";

const router = useRouter();
const route = useRoute();

const animes = ref<Page<Anime> | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

const fetchAnimes = async () => {
  try {
    loading.value = true;
    error.value = null;
    animes.value = await getAnimeList(animeListParams.value);
  } catch (err) {
    error.value = err instanceof Error ? err.message : "未知错误";
  } finally {
    loading.value = false;
  }
};

// 单一数据源：所有列表参数
const animeListParams = ref<AnimeListParams>({
  ...queryToFilters(route.query),
  pageSize: 30,
});

// 监听参数变化，同步更新 URL Query
watch(
  animeListParams,
  () => {
    router.push({ query: filtersToQuery(animeListParams.value) });
    fetchAnimes();
  },
  { deep: true, immediate: true },
);

// 处理分页变化
const handlePageChange = (page: number) => {
  animeListParams.value.page = page;
  window.scrollTo(0, 0);
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
    <AnimeFilter v-model="animeListParams" />

    <AnimeList
      :animes="animes"
      :loading="loading"
      :error="error"
      v-on:page-change="handlePageChange"
    />
  </div>
</template>
