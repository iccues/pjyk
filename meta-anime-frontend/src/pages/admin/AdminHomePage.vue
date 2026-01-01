<script setup lang="ts">
import { List } from "@element-plus/icons-vue";
import FetchDataCard from "@/components/admin/FetchDataCard.vue";
</script>

<template>
  <div class="p-6 max-w-[1200px] mx-auto grid grid-cols-1 md:grid-cols-2 gap-6">
    <!-- 页面导航卡片 -->
    <el-card class="hover:shadow-lg transition-shadow">
      <template #header>
        <div class="flex items-center gap-2">
          <el-icon><List /></el-icon>
          <span class="font-semibold text-lg">页面管理</span>
        </div>
      </template>
      <div class="space-y-3">
        <p class="text-gray-600 mb-4">管理动画数据和映射关系</p>
        <router-link
          to="/admin/list"
          class="text-blue-600 hover:text-blue-800 transition-colors inline-block"
        >
          动画列表管理
        </router-link>
      </div>
    </el-card>

    <!-- 数据抓取卡片 -->
    <FetchDataCard />
  </div>
</template>
