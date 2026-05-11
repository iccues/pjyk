<script setup lang="ts">
import AnimeListSection from "@/components/AnimeListSection.vue";
import MappingListSection from "@/components/MappingListSection.vue";
</script>

<template>
  <div class="mx-auto flex h-full max-w-[1800px] flex-col overflow-hidden p-5 pb-0">
    <el-row :gutter="24" class="flex-1 overflow-hidden">
      <!-- 左列：动画列表 -->
      <el-col :xs="24" :lg="12" class="h-full">
        <AnimeListSection />
      </el-col>

      <!-- 右列：未关联映射列表 -->
      <el-col :xs="24" :lg="12" class="h-full">
        <MappingListSection />
      </el-col>
    </el-row>
  </div>
</template>
