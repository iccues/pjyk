<script setup lang="ts">
import { useHead } from "@unhead/vue";

import MarkdownViewer from "@/components/MarkdownViewer.vue";
import raw from "@/docs/about.md?raw";
import { buildSeoHead } from "@/utils/seoUtils";

useHead(
  buildSeoHead({
    title: "关于 - 有希计划",
    description: "有希计划是一个整合 Bangumi、MyAnimeList、AniList 多平台数据的番剧综合评分平台。",
  }),
);
</script>

<template>
  <MarkdownViewer :raw="raw" />
</template>
