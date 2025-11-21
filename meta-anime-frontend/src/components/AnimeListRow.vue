<script setup lang="ts">
import AnimeList from './AnimeList.vue';
import type { Season } from '../types/anime';

defineProps<{
  title?: string;
  year?: number;
  season?: Season;
}>();
</script>

<template>
  <div class="w-full">
    <!-- 标题栏 -->
    <div class="flex justify-between items-center mb-8">
      <h2 class="text-3xl font-bold text-gray-900 border-l-4 border-blue-500 pl-4">
        {{ title }}
      </h2>
      <router-link :to="`/anime/list?year=${year || ''}&season=${season || ''}`"
        class="text-sm font-medium text-blue-600 hover:text-blue-500 flex items-center gap-1 transition-colors">
        查看更多 <span aria-hidden="true">&rarr;</span>
      </router-link>
    </div>
    <!-- 动画列表 -->
    <div class="h-[320px] overflow-hidden pt-2">
      <AnimeList :year="year" :season="season" :page-size="10" />
    </div>
  </div>
</template>
