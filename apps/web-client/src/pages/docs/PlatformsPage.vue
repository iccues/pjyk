<script setup lang="ts">
import { useHead } from "@unhead/vue";
import { buildSeoHead } from "@/utils/seoUtils";
import MarkdownViewer from "@/components/MarkdownViewer.vue";
import raw from "@/docs/platforms.md?raw";

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
