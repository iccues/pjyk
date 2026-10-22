<script setup lang="ts">
import { katex } from "@mdit/plugin-katex";
import { useHead } from "@unhead/vue";
import { computedAsync } from "@vueuse/core";
import MarkdownIt from "markdown-it";
import { computed } from "vue";

import { buildSeoHead } from "@/utils/seoUtils";

import "github-markdown-css/github-markdown-light.css";

// load 返回 md 原文，title 和 description 用于页面 SEO
const props = defineProps<{
  load: () => Promise<{ default: string }>;
  title: string;
  description: string;
}>();

const md = new MarkdownIt();
md.use(katex, { throwOnError: false, output: "mathml" });

// 渲染后的文档 HTML，load 变化时重新加载，并丢弃过期的结果
const content = computedAsync(async () => {
  const { default: raw } = await props.load();
  return md.render(raw);
}, "");

useHead(
  buildSeoHead({
    title: computed(() => props.title),
    description: computed(() => props.description),
  }),
);
</script>

<template>
  <div class="markdown-body" v-html="content"></div>
</template>

<style scoped>
/* github-markdown-css 会覆盖工具类，因此在组件内定义容器样式。 */
.markdown-body {
  box-sizing: border-box;
  max-width: 720px;
  margin-inline: auto;
  padding-block-start: 2.5rem;
  padding-inline: var(--page-gutter);
}

/* 块级公式超出时横向滚动，而非撑破布局 */
.markdown-body :deep(.katex-block) {
  overflow-x: auto;
  overflow-y: hidden;
}
</style>
