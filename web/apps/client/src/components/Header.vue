<script setup lang="ts">
import { Search } from "@element-plus/icons-vue";
import { ref } from "vue";
import { useRouter } from "vue-router";

import logo from "@/assets/logo.svg";

const router = useRouter();
const searchQuery = ref("");

const handleSearch = () => {
  const query = searchQuery.value.trim();
  const routeLocation = router.resolve({
    path: "/search",
    query: query ? { q: query } : {},
  });
  window.open(routeLocation.href, "_blank");
};
</script>

<template>
  <header>
    <div class="mx-auto flex max-w-[1240px] items-center justify-between px-4 py-5 sm:px-6">
      <router-link to="/" class="group flex cursor-pointer items-center gap-2">
        <img
          :src="logo"
          alt="Project Yuki Logo"
          class="h-8 w-8 rounded-full shadow-lg transition-shadow group-hover:shadow-indigo-500/30"
        />
        <span
          class="bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-xl font-bold text-transparent"
        >
          有希计划
        </span>
      </router-link>

      <!-- Header Search Box -->
      <div
        class="flex w-64 items-center overflow-hidden rounded-full border border-gray-300 bg-white transition-shadow"
      >
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索番剧..."
          class="w-full border-none bg-transparent px-4 py-1.5 text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none"
          @keyup.enter="handleSearch"
        />
        <button
          @click="handleSearch"
          class="mr-1 flex items-center justify-center p-1.5 text-gray-400 transition-colors hover:text-indigo-600 focus:outline-none"
          title="搜索"
        >
          <el-icon :size="16"><Search /></el-icon>
        </button>
      </div>
    </div>
  </header>
</template>
