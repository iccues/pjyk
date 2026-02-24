<script setup lang="ts">
import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import AnimeFilter from "@/components/public/AnimeFilter.vue";
import AnimeList from "@/components/public/AnimeList.vue";
import { getAnimeList, type AnimeListParams } from "@/api/public/anime";
import type { Page } from "@/types/page";
import { filtersToQuery, queryToFilters } from "@/utils/queryUtils";
import { useAnimeListHead } from "@/composables/public/useAnimeListHead";
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

// 动态 SEO 配置
useAnimeListHead(animeListParams);
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
