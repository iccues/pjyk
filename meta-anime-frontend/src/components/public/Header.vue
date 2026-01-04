<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import logo from "@/assets/logo.svg";

const isScrolled = ref(false);

const handleScroll = () => {
  isScrolled.value = window.scrollY > 10;
};

onMounted(() => {
  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});
</script>

<template>
  <header
    class="fixed top-0 left-0 right-0 z-50 transition-all duration-300 border-b border-transparent"
    :class="{ 'bg-white/70 backdrop-blur-md shadow-sm border-gray-100 py-3': isScrolled, 'bg-transparent py-5': !isScrolled }"
  >
    <div class="max-w-[1240px] mx-auto px-4 sm:px-6 flex items-center justify-between">
      <router-link
        to="/"
        class="flex items-center gap-2 cursor-pointer group"
      >
        <img :src="logo" alt="Meta Anime Logo" class="w-8 h-8 rounded-lg shadow-lg group-hover:shadow-indigo-500/30 transition-shadow" />
        <span class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-gray-800 to-gray-600">
          Meta Anime
        </span>
      </router-link>
    </div>
  </header>
</template>
