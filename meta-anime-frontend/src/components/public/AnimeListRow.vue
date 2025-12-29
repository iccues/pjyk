<script setup lang="ts">
import { onMounted, ref } from 'vue';
import AnimeCard from "./AnimeCard.vue";
import type { Season } from '@/types/anime';
import type { Anime } from "@/types/anime.ts";
import type { Page } from '@/types/page.ts';
import { getAnimeList } from '@/api/public/anime';

const props = defineProps<{
  title?: string;
  year?: number;
  season?: Season;
}>();

const animes = ref<Page<Anime> | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

const fetchAnimes = async () => {
  try {
    loading.value = true;
    error.value = null;
    animes.value = await getAnimeList({
      year: props.year,
      season: props.season,
      page: 1,
      pageSize: 12,
    });
  } catch (err) {
    error.value = err instanceof Error ? err.message : '未知错误';
  } finally {
    loading.value = false;
  }
};

onMounted(fetchAnimes);
</script>

<template>
  <!-- 标题栏 -->
  <div class="flex max-w-[1400px] mx-auto px-5 justify-between items-center mb-8">
    <h2 class="text-3xl font-bold text-gray-900 border-l-4 border-blue-500 pl-4">
      {{ title }}
    </h2>
    <router-link :to="`/anime/list?year=${year || ''}&season=${season || ''}`"
      class="text-sm font-medium text-blue-600 hover:text-blue-500 flex items-center gap-1 transition-colors">
      查看更多 <span aria-hidden="true">&rarr;</span>
    </router-link>
  </div>

  <!-- 动画列表 -->
  <div v-if="error" class="text-center py-10 text-base text-red-600">{{ error }}</div>
  <div v-else-if="loading" class="text-center py-10 text-base text-gray-600">加载中...</div>

  <div
    v-else-if="animes && animes.content.length > 0"
    class="max-w-[1400px] mx-auto flex gap-5 overflow-x-auto pb-4 px-5 scroll-smooth scrollbar-hide"
  >
    <AnimeCard
      v-for="anime in animes.content"
      :key="anime.animeId"
      :anime="anime"
      class="flex-shrink-0"
    />
  </div>

  <div v-else class="text-center py-10 text-base text-gray-600">暂无数据</div>
</template>

<style scoped>
/* 隐藏滚动条但保持滚动功能 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.scrollbar-hide {
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}
</style>
