<script setup lang="ts">
import Header from "@/components/Header.vue";
</script>

<template>
  <Header />

  <main>
    <router-view />
  </main>

  <footer class="w-full py-6 text-center text-sm text-gray-500">
    <nav class="mb-3 flex justify-center gap-6">
      <RouterLink to="/docs/about" class="transition-colors hover:text-gray-800">关于</RouterLink>
      <RouterLink to="/docs/metric" class="transition-colors hover:text-gray-800"
        >评分说明</RouterLink
      >
      <RouterLink to="/docs/platforms" class="transition-colors hover:text-gray-800"
        >平台参数</RouterLink
      >
    </nav>
    <p>&copy; 2026 Project Yuki. All rights reserved.</p>
  </footer>
</template>
