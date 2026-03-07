<script setup lang="ts">
import logo from "@/assets/logo.svg";
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
    </div>
  </header>
</template>
