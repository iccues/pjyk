<script setup lang="ts">
import { SEASON_NAME_MAP } from "@pjyk-web/shared/constants/ui-options.ts";

import AnimeListRow from "@/components/AnimeListRow.vue";

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
</script>

<template>
  <div class="space-y-16 pt-5 pb-20">
    <!-- 本季最佳 -->
    <AnimeListRow
      :title="`${currentYear}年${SEASON_NAME_MAP[currentSeason]}最佳`"
      :year="currentYear"
      :season="currentSeason"
    />
    <!-- 当季最高人气 -->
    <AnimeListRow
      :title="`${currentYear}年${SEASON_NAME_MAP[currentSeason]}最热`"
      :year="currentYear"
      :season="currentSeason"
      sort-by="POPULARITY"
    />
    <!-- 年度最佳 -->
    <AnimeListRow :title="`${yearlyBestYear}年度最佳`" :year="yearlyBestYear" />
    <!-- 历史最佳 -->
    <AnimeListRow title="历史最佳" />
  </div>
</template>
