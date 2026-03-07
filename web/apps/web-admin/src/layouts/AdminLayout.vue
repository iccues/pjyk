<script setup lang="ts">
import { User } from "@element-plus/icons-vue";
</script>

<template>
  <div class="flex h-screen flex-col">
    <header
      class="flex items-center justify-between border-b border-gray-100 bg-white px-6 py-4 shadow-sm"
    >
      <div class="flex items-center gap-8">
        <router-link
          to="/"
          class="text-lg font-medium text-gray-600 no-underline transition-colors hover:text-blue-600"
          title="返回后台首页"
        >
          Admin Panel
        </router-link>
      </div>

      <router-link
        to="/auth"
        class="flex items-center justify-center rounded-full p-2 text-gray-600 transition-colors hover:bg-gray-100 hover:text-blue-600"
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
