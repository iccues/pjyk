<script setup lang="ts">
// 仅处理输入和提交，跳转方式由调用方决定。
const keyword = defineModel<string>({ required: true });

defineEmits<{ submit: [] }>();

withDefaults(defineProps<{ placeholder?: string }>(), { placeholder: "搜索动画…" });
</script>

<template>
  <form
    class="flex w-full max-w-md items-center rounded-xl border border-gray-500 bg-white focus-within:outline-2 focus-within:outline-offset-2 focus-within:outline-gray-900"
    @submit.prevent="$emit('submit')"
  >
    <!-- 使用 text 避免原生清除按钮绕过提交；触屏下使用 16px 字号防止 iOS 缩放。 -->
    <input
      v-model="keyword"
      type="text"
      :placeholder="placeholder"
      aria-label="搜索动画"
      class="w-full min-w-0 border-none bg-transparent px-4 py-2.5 text-[15px] text-gray-900 placeholder:text-gray-400 focus:outline-none pointer-coarse:text-base"
    />
    <button
      type="submit"
      class="m-1 shrink-0 rounded-lg bg-gray-900 px-4 py-2 text-[14px] font-medium text-white transition-colors hover:bg-gray-700"
    >
      搜索
    </button>
  </form>
</template>
