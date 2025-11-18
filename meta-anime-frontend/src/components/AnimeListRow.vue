<script setup lang="ts">
import AnimeList from './AnimeList.vue';
import type { Season } from '../types/anime';
import { ArrowRight } from '@element-plus/icons-vue';

defineProps<{
  title?: string;
  year?: number;
  season?: Season;
}>();
</script>

<template>
  <div class="w-full">
    <!-- 标题栏 -->
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-xl font-semibold text-gray-800 m-0">{{ title }}</h2>
      <a
        :href="`/anime/list?year=${year || ''}&season=${season || ''}`"
        class="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-700 no-underline transition-colors"
      >
        <span>查看更多</span>
        <el-icon><ArrowRight /></el-icon>
      </a>
    </div>

    <div class="h-[320px] overflow-hidden pt-2">
      <AnimeList
        :year="year"
        :season="season"
        :page-size="10"
      />
    </div>
  </div>
</template>
