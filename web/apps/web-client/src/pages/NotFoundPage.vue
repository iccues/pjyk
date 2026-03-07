<script setup lang="ts">
import { useHead } from "@unhead/vue";
import { useRouter } from "vue-router";

import { buildSeoHead } from "@/utils/seoUtils";

const router = useRouter();

useHead(
  buildSeoHead({
    title: "页面不存在 - 有希计划",
    description: "你访问的页面不存在。",
  }),
);
</script>

<template>
  <div class="flex flex-col items-center justify-center py-20 text-center">
    <div class="max-w-md space-y-6">
      <!-- 大号 404 -->
      <p class="text-[8rem] leading-none font-extrabold text-gray-200 select-none">404</p>

      <!-- 标题 -->
      <h1 class="text-2xl font-bold text-gray-800">页面不存在</h1>

      <!-- 描述 -->
      <p class="text-sm leading-relaxed text-gray-500">
        你访问的页面可能已被删除、移动，或从未存在过。
      </p>

      <!-- 操作按钮 -->
      <div class="flex justify-center gap-3 pt-2">
        <button
          class="rounded-full bg-gray-800 px-5 py-2 text-sm font-medium text-white transition-colors hover:bg-gray-700"
          @click="router.push('/')"
        >
          返回首页
        </button>
        <button
          class="rounded-full border border-gray-300 px-5 py-2 text-sm font-medium text-gray-600 transition-colors hover:bg-gray-100"
          @click="router.back()"
        >
          返回上一页
        </button>
      </div>
    </div>
  </div>
</template>
