<script setup lang="ts">
import { useQuery } from "@urql/vue";

import AnimeFilter from "@/components/AnimeFilter.vue";
import AnimeList from "@/components/AnimeList.vue";
import { useAnimeListHead } from "@/composables/useAnimeListHead";
import { useAnimeListQuery } from "@/composables/useAnimeListQuery";
import { GetAnimeListDocument } from "@/graphql/generated/graphql";

const { animeListParams, filtersModel, handlePageChange } = useAnimeListQuery();

const { data, fetching, error } = useQuery({
  query: GetAnimeListDocument,
  variables: animeListParams,
});

// 动态 SEO 配置
useAnimeListHead(animeListParams);
</script>

<template>
  <div class="mx-auto max-w-[1400px] p-5">
    <!-- 筛选器 -->
    <AnimeFilter v-model="filtersModel" />

    <AnimeList
      :animeList="data?.animeList"
      :fetching="fetching"
      :error="error"
      v-on:page-change="handlePageChange"
    />
  </div>
</template>
