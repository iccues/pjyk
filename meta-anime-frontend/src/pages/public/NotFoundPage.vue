<script setup lang="ts">
import { useRouter } from "vue-router";
import { useHead } from "@unhead/vue";

const router = useRouter();

useHead({
  title: "页面不存在 - 有希计划",
  meta: [{ name: "description", content: "你访问的页面不存在。" }],
});
</script>

<template>
  <div class="flex flex-col items-center justify-center text-center py-20">
    <div class="space-y-6 max-w-md">
      <!-- 大号 404 -->
      <p class="text-[8rem] font-extrabold leading-none text-gray-200 select-none">404</p>

      <!-- 标题 -->
      <h1 class="text-2xl font-bold text-gray-800">页面不存在</h1>

      <!-- 描述 -->
      <p class="text-gray-500 text-sm leading-relaxed">
        你访问的页面可能已被删除、移动，或从未存在过。
      </p>

      <!-- 操作按钮 -->
      <div class="flex gap-3 justify-center pt-2">
        <button
          class="px-5 py-2 rounded-full bg-gray-800 text-white text-sm font-medium hover:bg-gray-700 transition-colors"
          @click="router.push('/')"
        >
          返回首页
        </button>
        <button
          class="px-5 py-2 rounded-full border border-gray-300 text-gray-600 text-sm font-medium hover:bg-gray-100 transition-colors"
          @click="router.back()"
        >
          返回上一页
        </button>
      </div>
    </div>
  </div>
</template>
