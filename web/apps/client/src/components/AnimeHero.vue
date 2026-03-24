<script setup lang="ts">
import { computed } from "vue";

import type { GetAnimeDetailQuery } from "@/graphql/generated/graphql";

const props = defineProps<{
  anime: NonNullable<GetAnimeDetailQuery["anime"]>;
}>();

const displayTitle = computed(() => {
  const t = props.anime.title;
  return t.titleCn || t.titleNative || t.titleRomaji || t.titleEn || "";
});

const subTitles = computed(() => {
  const t = props.anime.title;
  const arr: string[] = [];
  if (t.titleCn && t.titleNative && t.titleNative !== t.titleCn) arr.push(t.titleNative);
  if (t.titleRomaji) arr.push(t.titleRomaji);
  if (t.titleEn) arr.push(t.titleEn);
  return arr;
});
</script>

<template>
  <div class="flex flex-col items-center gap-8 sm:flex-row sm:items-start">
    <!-- Cover Image -->
    <div v-if="anime.coverImage" class="shrink-0">
      <img
        :src="anime.coverImage"
        :alt="displayTitle"
        class="w-[16rem] rounded-2xl object-cover shadow-lg sm:w-60"
      />
    </div>

    <!-- Info -->
    <div
      class="flex flex-col items-center justify-center gap-3 text-center sm:items-start sm:justify-start sm:text-left"
    >
      <!-- Main title -->
      <h1 class="m-0 text-[24px] leading-tight font-bold text-gray-900 sm:text-[30px]">
        {{ displayTitle }}
      </h1>

      <!-- Sub titles -->
      <div v-if="subTitles.length > 0" class="flex flex-col gap-1">
        <p v-for="(t, i) in subTitles" :key="i" class="m-0 text-[14px] text-gray-500">
          {{ t }}
        </p>
      </div>

      <!-- Meta tags -->
      <div
        v-if="anime.startDate"
        class="mt-2 flex flex-wrap items-center justify-center gap-2 text-[14px] text-gray-400 sm:justify-start"
      >
        <span>{{ anime.startDate }} 开播</span>
      </div>

      <!-- Stats Row -->
      <div class="mt-4 flex flex-wrap items-center justify-center gap-6 sm:justify-start">
        <div
          v-if="anime.averageScore"
          class="flex flex-col items-center text-indigo-600 sm:items-start"
        >
          <span class="text-[12px] font-bold tracking-wider uppercase opacity-60">综合评分</span>
          <div class="flex items-baseline">
            <span class="text-[32px] leading-none font-black">{{
              anime.averageScore.toFixed(1)
            }}</span>
            <span class="ml-1 text-[16px] font-medium text-gray-400">/100</span>
          </div>
        </div>
        <div
          v-if="anime.popularity"
          class="flex flex-col items-center text-pink-500 sm:items-start"
        >
          <span class="text-[12px] font-bold tracking-wider uppercase opacity-60">人气值</span>
          <span class="text-[32px] leading-none font-black">{{ anime.popularity.toFixed(0) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
