<script setup lang="ts">
import { useHead } from "@unhead/vue";
import MarkdownViewer from "@/components/public/MarkdownViewer.vue";
import raw from "@/docs/metric.md?raw";

useHead({
  title: "评分与热度说明 - 有希计划",
  meta: [
    {
      name: "description",
      content: "了解有希计划的番剧评分与热度数据是如何通过多平台归一化处理后综合计算得出的。",
    },
  ],
});
</script>

<template>
  <MarkdownViewer :raw="raw" />
</template>
