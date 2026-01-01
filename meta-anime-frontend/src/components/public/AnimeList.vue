<script setup lang="ts">
import { ref, watch } from "vue";
import { type AnimeListParams, getAnimeList } from "@/api/public/anime";
import type { Anime } from "@/types/anime.ts";
import type { Page, PageInfo } from "@/types/page.ts";
import AnimeCard from "./AnimeCard.vue";

const props = defineProps<AnimeListParams>();
const emit =
  defineEmits<(event: "update:pageInfo", payload: PageInfo) => void>();

const animes = ref<Page<Anime> | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

const fetchAnimes = async () => {
  try {
    loading.value = true;
    error.value = null;
    animes.value = await getAnimeList(props);
    emit("update:pageInfo", animes.value.page);
  } catch (err) {
    error.value = err instanceof Error ? err.message : "未知错误";
  } finally {
    loading.value = false;
  }
};

watch(
  () => [props.year, props.season, props.page, props.pageSize],
  fetchAnimes,
  { immediate: true },
);
</script>

<template>
  <div v-if="error" class="text-center py-10 text-base text-red-600">{{ error }}</div>
  <div v-else-if="loading" class="text-center py-10 text-base text-gray-600">加载中...</div>
  <div v-else-if="animes && animes.content.length > 0" class="grid grid-cols-[repeat(auto-fill,12.5rem)] gap-5 justify-center">
    <AnimeCard v-for="anime in animes.content" :key="anime.animeId" :anime="anime"/>
  </div>
  <div v-else class="text-center py-10 text-base text-gray-600">暂无数据</div>
</template>
