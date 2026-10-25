<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

import LinkButton from "./LinkButton.vue";
import SearchBox from "./SearchBox.vue";

const router = useRouter();
const keyword = ref("");

const quickLinkClass = "rounded-lg px-3 py-1.5 text-[14px] font-medium";

const handleSearch = () => {
  const q = keyword.value.trim();
  // 空关键词不提交。
  if (!q) return;

  const routeLocation = router.resolve({ path: "/search", query: { q } });
  window.open(routeLocation.href, "_blank");
};
</script>

<template>
  <section>
    <div class="container-page py-12 text-center sm:py-16">
      <h1 class="m-0 text-[26px] leading-tight font-bold text-gray-900 sm:text-[32px]">
        更全面的动画评分
      </h1>
      <p class="mx-auto mt-3 max-w-xl text-[15px] leading-relaxed text-gray-600">
        有希计划整合 Bangumi、MyAnimeList、AniList 的评分与热度数据，归一化后给出统一的综合评价。
      </p>

      <SearchBox v-model="keyword" class="mx-auto mt-7" @submit="handleSearch" />

      <div class="mt-4 flex flex-wrap items-center justify-center gap-1">
        <LinkButton to="/anime/list" :class="quickLinkClass">全部动画</LinkButton>
        <LinkButton to="/docs/metric" :class="quickLinkClass">评分说明</LinkButton>
      </div>
    </div>
  </section>
</template>
