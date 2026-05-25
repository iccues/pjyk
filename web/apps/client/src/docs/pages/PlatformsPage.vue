<script setup lang="ts">
import { useHead } from "@unhead/vue";

import raw from "@/docs/content/platforms.md?raw";
import MarkdownViewer from "@/docs/MarkdownViewer.vue";
import { buildSeoHead } from "@/utils/seoUtils";

useHead(
  buildSeoHead({
    title: "平台数据参数 - 有希计划",
    description: "查看有希计划各评分平台的权重、评分均值、标准差及热度倍率等数据参数。",
  }),
);
</script>

<template>
  <MarkdownViewer :raw="raw" />
</template>
