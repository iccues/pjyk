<script setup lang="ts">
import Header from "@/components/public/Header.vue";
</script>

<template>
  <Header />
  
  <main class="pt-20">
    <router-view />
  </main>

  <footer class="py-6 text-center text-sm text-gray-500 w-full">
    <p>&copy; 2025 Meta Anime. All rights reserved.</p>
  </footer>
</template>
