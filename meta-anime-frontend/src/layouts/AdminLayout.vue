<script setup lang="ts">
import { User } from "@element-plus/icons-vue";
</script>

<template>
  <div class="h-screen flex flex-col">
    <header class="flex justify-between items-center px-6 py-4 bg-white shadow-sm border-b border-gray-100">
      <div class="flex items-center gap-8">
        <router-link
          to="/"
          class="text-lg font-bold text-gray-800 hover:text-blue-600 transition-colors no-underline"
          title="返回前台首页"
        >
          Meta Anime
        </router-link>

        <router-link
          to="/admin"
          class="text-lg font-medium text-gray-600 hover:text-blue-600 transition-colors no-underline"
          title="返回后台首页"
        >
          Admin Panel
        </router-link>
      </div>

      <router-link
        to="/admin/auth"
        class="text-gray-600 hover:text-blue-600 transition-colors flex items-center justify-center p-2 rounded-full hover:bg-gray-100"
        title="用户信息"
      >
        <el-icon size="20"><User /></el-icon>
      </router-link>
    </header>

    <div class="flex-1 overflow-hidden">
      <router-view />
    </div>
  </div>
</template>
