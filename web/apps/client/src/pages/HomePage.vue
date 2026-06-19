<script setup lang="ts">
import { SEASON_NAME_MAP } from "@pjyk-web/shared/constants/ui-options.ts";
import { useQuery } from "@urql/vue";
import type { RouteLocationRaw } from "vue-router";

import AnimeListRow from "@/components/AnimeListRow.vue";
import {
  GetHomeAnimeRowsDocument,
  type GetAnimeListQueryVariables,
} from "@/graphql/generated/graphql";
import { filtersToQuery } from "@/utils/queryUtils";

// 获取当前年份和季度
const now = new Date();
now.setMonth(now.getMonth() - 1);
const currentYear = now.getFullYear();
const currentMonth = now.getMonth() + 1; // JavaScript 月份从0开始

// 根据月份确定当前季度
const getCurrentSeason = (): "WINTER" | "SPRING" | "SUMMER" | "FALL" => {
  if (currentMonth >= 1 && currentMonth <= 3) return "WINTER";
  if (currentMonth >= 4 && currentMonth <= 6) return "SPRING";
  if (currentMonth >= 7 && currentMonth <= 9) return "SUMMER";
  if (currentMonth >= 10 && currentMonth <= 12) return "FALL";
  return "SPRING";
};

const currentSeason = getCurrentSeason();
const yearlyBestYear = currentSeason === "WINTER" ? currentYear - 1 : currentYear;

const createAnimeListLink = (filters: GetAnimeListQueryVariables = {}): RouteLocationRaw => ({
  path: "/anime/list",
  query: filtersToQuery(filters),
});

const seasonBestLink = createAnimeListLink({ year: currentYear, season: currentSeason });
const seasonPopularLink = createAnimeListLink({
  year: currentYear,
  season: currentSeason,
  sortBy: "POPULARITY",
});
const yearlyBestLink = createAnimeListLink({ year: yearlyBestYear });
const allTimeBestLink = createAnimeListLink();

const { data, fetching, error } = useQuery({
  query: GetHomeAnimeRowsDocument,
  variables: {
    currentYear,
    currentSeason,
    yearlyBestYear,
  },
});
</script>

<template>
  <div class="space-y-16 pt-5 pb-20">
    <div v-if="error" class="py-10 text-center text-base text-red-600">{{ error }}</div>

    <template v-else>
      <!-- 本季最佳 -->
      <AnimeListRow
        :title="`${currentYear}年${SEASON_NAME_MAP[currentSeason]}最佳`"
        :more-link="seasonBestLink"
        :animeList="data?.seasonBest.content"
        :fetching="fetching"
      />
      <!-- 当季最高人气 -->
      <AnimeListRow
        :title="`${currentYear}年${SEASON_NAME_MAP[currentSeason]}最热`"
        :more-link="seasonPopularLink"
        :animeList="data?.seasonPopular.content"
        :fetching="fetching"
      />
      <!-- 年度最佳 -->
      <AnimeListRow
        :title="`${yearlyBestYear}年度最佳`"
        :more-link="yearlyBestLink"
        :animeList="data?.yearlyBest.content"
        :fetching="fetching"
      />
      <!-- 历史最佳 -->
      <AnimeListRow
        title="历史最佳"
        :more-link="allTimeBestLink"
        :animeList="data?.allTimeBest.content"
        :fetching="fetching"
      />
    </template>
  </div>
</template>
