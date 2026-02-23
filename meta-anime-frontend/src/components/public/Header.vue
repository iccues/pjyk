<script setup lang="ts">
import logo from "@/assets/logo.svg";
</script>

<template>
  <header>
    <div class="max-w-[1240px] mx-auto px-4 sm:px-6 flex items-center justify-between py-5">
      <router-link to="/" class="flex items-center gap-2 cursor-pointer group">
        <img
          :src="logo"
          alt="Project Yuki Logo"
          class="w-8 h-8 rounded-full shadow-lg group-hover:shadow-indigo-500/30 transition-shadow"
        />
        <span
          class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-gray-800 to-gray-600"
        >
          有希计划
        </span>
      </router-link>
    </div>
  </header>
</template>
