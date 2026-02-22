<script setup lang="ts">
import { useHead } from "@unhead/vue";
import MarkdownViewer from "@/components/public/MarkdownViewer.vue";
import raw from "@/docs/platforms.md?raw";

useHead({
  title: "平台数据参数 - 有希计划",
  meta: [
    {
      name: "description",
      content: "查看有希计划各评分平台的权重、评分均值、标准差及热度倍率等数据参数。",
    },
  ],
});
</script>

<template>
  <MarkdownViewer :raw="raw" />
</template>
