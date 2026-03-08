<script setup lang="ts">
import { List } from "@element-plus/icons-vue";

import DataManagementCard from "@/components/DataManagementCard.vue";
import FetchDataCard from "@/components/FetchDataCard.vue";
</script>

<template>
  <div class="mx-auto grid max-w-[1200px] grid-cols-1 gap-6 p-6 md:grid-cols-2">
    <!-- 页面导航卡片 -->
    <el-card class="transition-shadow hover:shadow-lg">
      <template #header>
        <div class="flex items-center gap-2">
          <el-icon><List /></el-icon>
          <span class="text-lg font-semibold">页面管理</span>
        </div>
      </template>
      <div class="space-y-3">
        <p class="mb-4 text-gray-600">管理动画数据和映射关系</p>
        <router-link
          to="/list"
          class="inline-block text-blue-600 transition-colors hover:text-blue-800"
        >
          动画列表管理
        </router-link>
      </div>
    </el-card>

    <!-- 数据抓取卡片 -->
    <FetchDataCard />

    <!-- 数据管理卡片 -->
    <DataManagementCard />
  </div>
</template>
