<script setup lang="ts">
import { useHead } from "@unhead/vue";

import MarkdownViewer from "@/components/MarkdownViewer.vue";
import raw from "@/docs/metric.md?raw";
import { buildSeoHead } from "@/utils/seoUtils";

useHead(
  buildSeoHead({
    title: "评分与热度说明 - 有希计划",
    description: "了解有希计划的番剧评分与热度数据是如何通过多平台归一化处理后综合计算得出的。",
  }),
);
</script>

<template>
  <MarkdownViewer :raw="raw" />
</template>
