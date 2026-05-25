<script setup lang="ts">
import type { CombinedError } from "@urql/vue";

import type { AnimeListFragment } from "@/graphql/generated/graphql";

import AnimeCard from "./AnimeCard.vue";
import AnimeCardSkeleton from "./AnimeCardSkeleton.vue";

const props = defineProps<{
  animeList?: AnimeListFragment;
  fetching: boolean;
  error?: CombinedError;
}>();

const emit = defineEmits<{
  "page-change": [page: number];
}>();

const skeletonCount = 6;
</script>

<template>
  <div v-if="error" class="py-10 text-center text-base text-red-600">{{ error }}</div>
  <div v-else-if="fetching">
    <div class="grid grid-cols-[repeat(auto-fill,12.5rem)] justify-center gap-5">
      <AnimeCardSkeleton v-for="index in skeletonCount" :key="index" />
    </div>
  </div>
  <div v-else-if="animeList && animeList.content.length > 0">
    <div class="grid grid-cols-[repeat(auto-fill,12.5rem)] justify-center gap-5">
      <AnimeCard v-for="anime in animeList.content" :key="anime.animeId" :anime="anime" />
    </div>

    <div
      v-if="animeList.pageInfo && animeList.pageInfo.totalPages > 1"
      class="mt-8 flex flex-wrap items-center justify-center gap-2"
    >
      <el-pagination
        :current-page="(animeList.pageInfo.number || 0) + 1"
        :page-size="animeList.pageInfo.size"
        :total="animeList.pageInfo.totalElements"
        :page-count="animeList.pageInfo.totalPages"
        layout="prev, pager, next"
        @current-change="emit('page-change', $event - 1)"
      />
      <span class="text-[14px] text-gray-500">共 {{ animeList.pageInfo.totalElements }} 部</span>
    </div>
  </div>
  <div v-else class="py-10 text-center text-base text-gray-600">暂无数据</div>
</template>
