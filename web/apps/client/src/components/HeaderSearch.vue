<script setup lang="ts">
import { Search, Close } from "@element-plus/icons-vue";
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const searchQuery = ref("");
const isSearchVisible = ref(false);
const mobileInputRef = ref<HTMLInputElement | null>(null);

const toggleSearch = () => {
  isSearchVisible.value = !isSearchVisible.value;
  if (isSearchVisible.value) {
    setTimeout(() => {
      mobileInputRef.value?.focus();
    }, 100);
  }
};

const handleSearch = () => {
  const query = searchQuery.value.trim();
  const routeLocation = router.resolve({
    path: "/search",
    query: query ? { q: query } : {},
  });
  window.open(routeLocation.href, "_blank");
  isSearchVisible.value = false;
};
</script>

<template>
  <!-- Desktop Search Box -->
  <div
    class="hidden w-64 items-center overflow-hidden rounded-lg border border-gray-300 bg-white transition-shadow sm:flex"
  >
    <input
      v-model="searchQuery"
      type="text"
      placeholder="搜索番剧..."
      class="w-full border-none bg-transparent px-2.5 py-1.5 text-[12px] text-gray-900 placeholder:text-gray-400 focus:outline-none"
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

  <!-- Mobile Search Toggle -->
  <button
    @click="toggleSearch"
    class="flex h-10 w-10 items-center justify-center rounded-full text-gray-600 transition-colors focus:outline-none sm:hidden"
    title="打开搜索"
  >
    <el-icon :size="20"><Search /></el-icon>
  </button>

  <!-- Mobile Search Overlay -->
  <transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div
      v-if="isSearchVisible"
      class="fixed inset-0 z-10 flex flex-col bg-white px-3 py-6 sm:hidden"
    >
      <button
        @click="toggleSearch"
        class="mb-4 flex h-10 w-10 self-end text-gray-600"
      >
        <el-icon :size="24">
          <Close />
        </el-icon>
      </button>
      <div
        class="flex items-center overflow-hidden rounded-2xl border border-gray-300 bg-gray-50 transition-shadow mx-3"
      >
        <input
          ref="mobileInputRef"
          v-model="searchQuery"
          type="text"
          placeholder="搜索番剧..."
          class="w-full border-none bg-transparent px-4 py-3 text-[16px] text-gray-900 placeholder:text-gray-400 focus:outline-none"
          @keyup.enter="handleSearch"
        />
        <button
          @click="handleSearch"
          class="mr-2 flex items-center justify-center p-2 text-gray-400 transition-colors hover:text-indigo-600 focus:outline-none"
        >
          <el-icon :size="20"><Search /></el-icon>
        </button>
      </div>
    </div>
  </transition>
</template>
