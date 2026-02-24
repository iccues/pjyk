<script setup lang="ts">
import type { Anime } from "@/types/anime.ts";
import type { Page } from "@/types/page.ts";
import AnimeCard from "./AnimeCard.vue";

const props = defineProps<{
  animes: Page<Anime> | null;
  loading: boolean;
  error: string | null;
}>();

const emit = defineEmits<{
  "page-change": [page: number];
}>();
</script>

<template>
  <div v-if="error" class="text-center py-10 text-base text-red-600">{{ error }}</div>
  <div v-else-if="loading" class="text-center py-10 text-base text-gray-600">加载中...</div>
  <div
    v-else-if="animes && animes.content.length > 0"
    class="grid grid-cols-[repeat(auto-fill,12.5rem)] gap-5 justify-center"
  >
    <AnimeCard v-for="anime in animes.content" :key="anime.animeId" :anime="anime" />
  </div>
  <div v-else class="text-center py-10 text-base text-gray-600">暂无数据</div>

  <div v-if="animes?.page" class="flex flex-wrap justify-center items-center gap-2 mt-8">
    <el-pagination
      :current-page="(animes.page.number || 0) + 1"
      :page-size="animes.page.size"
      :total="animes.page.totalElements"
      :page-count="animes.page.totalPages"
      layout="prev, pager, next"
      @current-change="emit('page-change', $event - 1)"
    />
    <span class="text-[14px] text-gray-500">共 {{ animes.page.totalElements }} 部</span>
  </div>
</template>
