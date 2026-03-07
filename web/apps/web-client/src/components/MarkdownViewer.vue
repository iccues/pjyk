<script setup lang="ts">
import mk from "@vscode/markdown-it-katex";
import MarkdownIt from "markdown-it";

import "katex/dist/katex.min.css";
import "github-markdown-css/github-markdown-light.css";

const props = defineProps<{
  raw: string;
}>();

const md = new MarkdownIt();
md.use(mk, { throwOnError: false });
const content = md.render(props.raw);
</script>

<template>
  <div class="markdown-body" v-html="content"></div>
</template>

<style scoped>
.markdown-body {
  max-width: 720px;
  margin: 5rem auto;
  padding: 0 1.5rem 4rem;
  box-sizing: border-box;
}

/* 块级公式超出时横向滚动，而非撑破布局 */
.markdown-body :deep(.katex-display) {
  overflow-x: auto;
  overflow-y: hidden;
}
</style>
