<script setup lang="ts">
import { Search } from "@element-plus/icons-vue";
import bilibiliIcon from "@pjyk-web/shared/assets/bilibili.svg";
import githubIcon from "@pjyk-web/shared/assets/github.svg";
import douyinIcon from "@pjyk-web/shared/assets/tiktok.svg";
import xiaohongshuIcon from "@pjyk-web/shared/assets/xiaohongshu.svg";

import logo from "@/assets/logo.svg";
import LinkButton from "@/components/LinkButton.vue";
</script>

<template>
  <!-- main 撑满剩余视口高度，使页脚保持在底部。 -->
  <div class="flex min-h-dvh flex-col">
    <header>
      <div class="container-page flex items-center justify-between py-3.5">
        <!-- Logo 为装饰图，链接名称由文字提供。 -->
        <router-link to="/" class="flex cursor-pointer items-center gap-2">
          <img :src="logo" alt="" class="h-8 w-8" />
          <span
            class="bg-linear-to-r from-gray-800 to-gray-600 bg-clip-text text-xl font-bold text-transparent"
          >
            有希计划
          </span>
        </router-link>

        <!-- 搜索入口统一跳转到搜索页。 -->
        <LinkButton to="/search" class="size-10 justify-center rounded-[10px]" aria-label="搜索">
          <el-icon :size="20"><Search /></el-icon>
        </LinkButton>
      </div>
    </header>

    <main class="flex-1">
      <router-view />
    </main>

    <footer class="mt-16 w-full py-8 text-center text-sm text-gray-500">
      <nav class="mb-4 flex justify-center gap-6">
        <RouterLink to="/docs/about" class="transition-colors hover:text-gray-800">关于</RouterLink>
        <RouterLink to="/docs/metric" class="transition-colors hover:text-gray-800"
          >评分说明</RouterLink
        >
        <RouterLink to="/docs/platforms" class="transition-colors hover:text-gray-800"
          >平台参数</RouterLink
        >
      </nav>
      <!-- 站外链接单独成行，避免与文字导航挤在同一行过长。 -->
      <div class="mb-4 flex items-center justify-center gap-5">
        <a
          href="https://github.com/iccues/pjyk"
          target="_blank"
          rel="noopener noreferrer"
          class="opacity-60 transition-opacity hover:opacity-100"
          aria-label="GitHub"
        >
          <img :src="githubIcon" class="size-5" />
        </a>
        <a
          href="https://space.bilibili.com/3747563725393689"
          target="_blank"
          rel="noopener noreferrer"
          class="opacity-60 transition-opacity hover:opacity-100"
          aria-label="Bilibili"
        >
          <img :src="bilibiliIcon" class="size-5" />
        </a>
        <a
          href="https://www.xiaohongshu.com/user/profile/6ac6868f0000000013023400"
          target="_blank"
          rel="noopener noreferrer"
          class="opacity-60 transition-opacity hover:opacity-100"
          aria-label="小红书"
        >
          <img :src="xiaohongshuIcon" class="-my-3 size-11" />
        </a>
        <a
          href="https://www.douyin.com/user/MS4wLjABAAAA7lzAJd6IKK5-szycGau99i-X_cP-XnWreqsAiMHuzEurGW7u5fiwPDtRP7DEaRVu"
          target="_blank"
          rel="noopener noreferrer"
          class="opacity-60 transition-opacity hover:opacity-100"
          aria-label="抖音"
        >
          <img :src="douyinIcon" class="size-5" />
        </a>
      </div>
      <p>&copy; 2026 Project Yuki. All rights reserved.</p>
    </footer>
  </div>
</template>
