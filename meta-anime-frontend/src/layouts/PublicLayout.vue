<script setup lang="ts">
import Header from "@/components/public/Header.vue";
</script>

<template>
  <Header />

  <main class="pt-20">
    <router-view />
  </main>

  <footer class="py-6 text-center text-sm text-gray-500 w-full">
    <nav class="flex justify-center gap-6 mb-3">
      <RouterLink to="/docs/about" class="hover:text-gray-800 transition-colors">关于</RouterLink>
      <RouterLink to="/docs/metric" class="hover:text-gray-800 transition-colors"
        >评分说明</RouterLink
      >
      <RouterLink to="/docs/platforms" class="hover:text-gray-800 transition-colors"
        >平台参数</RouterLink
      >
    </nav>
    <p>&copy; 2026 Project Yuki. All rights reserved.</p>
  </footer>
</template>
