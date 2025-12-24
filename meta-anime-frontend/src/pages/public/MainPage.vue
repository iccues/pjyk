<script setup lang="ts">
import AnimeListRow from "@/components/public/AnimeListRow.vue";

// 获取当前年份和季度
const now = new Date();
const currentYear = now.getFullYear();
const currentMonth = now.getMonth() + 1; // JavaScript 月份从0开始

// 根据月份确定当前季度
const getCurrentSeason = (): 'WINTER' | 'SPRING' | 'SUMMER' | 'FALL' => {
  if (currentMonth >= 1 && currentMonth <= 3) return 'WINTER';
  if (currentMonth >= 4 && currentMonth <= 6) return 'SPRING';
  if (currentMonth >= 7 && currentMonth <= 9) return 'SUMMER';
  if (currentMonth >= 10 && currentMonth <= 12) return 'FALL';
  return 'SPRING';
};

const currentSeason = getCurrentSeason();

// 季度名称映射
const seasonNames: Record<string, string> = {
  WINTER: '冬季',
  SPRING: '春季',
  SUMMER: '夏季',
  FALL: '秋季'
};
</script>

<template>
  <div class="min-h-screen bg-gray-50/50">
    <div class="p-5 max-w-[1400px] mx-auto space-y-16 pb-20">
      <!-- 本季新番 -->
      <AnimeListRow
        :title="`${currentYear}年${seasonNames[currentSeason]}新番`"
        :year="currentYear"
        :season="currentSeason"
      />
      <!-- 本年新番 -->
      <AnimeListRow
        :title="`${currentYear}年新番`"
        :year="currentYear"
      />
      <!-- 历史最高 -->
      <AnimeListRow
        title="历史最高"
      />
    </div>
  </div>
</template>
