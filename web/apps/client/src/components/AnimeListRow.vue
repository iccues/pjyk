<script setup lang="ts">
import { ArrowLeftBold, ArrowRightBold } from "@element-plus/icons-vue";
import { useResizeObserver } from "@vueuse/core";
import { ref } from "vue";
import { RouterLink, type RouteLocationRaw } from "vue-router";

import type { AnimeCardFragment } from "@/graphql/generated/graphql";

import AnimeCard from "./AnimeCard.vue";
import AnimeCardSkeleton from "./AnimeCardSkeleton.vue";

defineProps<{
  title: string;
  moreLink: RouteLocationRaw;
  animeList?: AnimeCardFragment[];
  fetching: boolean;
}>();

const scrollContainer = ref<HTMLElement | null>(null);
const canScrollLeft = ref(false);
const canScrollRight = ref(false);
const skeletonCount = 6;

// 保留 1px 的滚动边界容差。
const updateScrollState = () => {
  const container = scrollContainer.value;
  if (!container) {
    canScrollLeft.value = false;
    canScrollRight.value = false;
    return;
  }

  const { scrollLeft, scrollWidth, clientWidth } = container;
  canScrollLeft.value = scrollLeft > 1;
  canScrollRight.value = scrollLeft + clientWidth < scrollWidth - 1;
};

const getCardStep = (container: HTMLElement) => {
  const first = container.children[0];
  const second = container.children[1];
  if (!(first instanceof HTMLElement)) return 0;

  return second instanceof HTMLElement
    ? second.getBoundingClientRect().left - first.getBoundingClientRect().left
    : first.getBoundingClientRect().width;
};

const scroll = (direction: -1 | 1) => {
  const container = scrollContainer.value;
  if (!container) return;

  const step = getCardStep(container);
  if (step <= 0) return;

  const position = container.scrollLeft / step;
  const index = direction < 0 ? Math.ceil(position) : Math.floor(position);

  container.scrollTo({ left: (index + direction * 2) * step, behavior: "smooth" });
};

// 容器挂载和尺寸变化时刷新滚动状态。
useResizeObserver(scrollContainer, updateScrollState);

const cardWidthClass = "w-[var(--card-width)] shrink-0";

const arrowClass =
  "inline-flex size-9 items-center justify-center rounded-full bg-gray-100 text-gray-700 transition-colors hover:bg-gray-200 hover:text-gray-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:cursor-not-allowed disabled:bg-gray-50 disabled:text-gray-300 disabled:hover:bg-gray-50";
</script>

<template>
  <div class="container-page mb-5 flex items-center justify-between gap-3">
    <h2 class="m-0 min-w-0 text-[22px] font-bold text-gray-900">
      <RouterLink
        :to="moreLink"
        class="group -m-1 inline-flex items-center gap-1.5 rounded p-1 text-gray-900 no-underline transition-colors hover:text-indigo-600 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
      >
        <span class="relative py-0.5">
          {{ title }}
          <span
            aria-hidden="true"
            class="absolute inset-x-0 bottom-0 h-0.5 origin-left scale-x-0 rounded-full bg-indigo-500/70 transition-transform duration-200 group-hover:scale-x-100"
          ></span>
        </span>
        <span
          aria-hidden="true"
          class="inline-flex shrink-0 text-gray-400 transition duration-200 group-hover:translate-x-0.5 group-hover:text-indigo-600"
        >
          <ArrowRightBold class="size-4.5" />
        </span>
      </RouterLink>
    </h2>

    <!-- 仅为支持精细指针的设备显示滚动按钮。 -->
    <div class="hidden shrink-0 items-center gap-2 any-pointer-fine:flex">
      <button
        :disabled="!canScrollLeft"
        @click="scroll(-1)"
        :class="arrowClass"
        aria-label="向左滚动"
      >
        <ArrowLeftBold class="size-5" />
      </button>
      <button
        :disabled="!canScrollRight"
        @click="scroll(1)"
        :class="arrowClass"
        aria-label="向右滚动"
      >
        <ArrowRightBold class="size-5" />
      </button>
    </div>
  </div>

  <div v-if="fetching">
    <div class="scrollbar-hide container-page-bleed flex gap-5 overflow-x-auto py-4">
      <AnimeCardSkeleton v-for="index in skeletonCount" :key="index" :class="cardWidthClass" />
    </div>
  </div>

  <!-- 垂直内边距避免卡片悬浮效果被滚动容器裁切。 -->
  <div
    v-else-if="animeList && animeList.length > 0"
    ref="scrollContainer"
    @scroll="updateScrollState"
    class="scrollbar-hide container-page-bleed flex gap-5 overflow-x-auto scroll-smooth py-4"
  >
    <AnimeCard
      v-for="anime in animeList"
      :key="anime.animeId"
      :anime="anime"
      :class="cardWidthClass"
    />
  </div>

  <div v-else class="py-10 text-center text-base text-gray-600">暂无数据</div>
</template>

<style scoped>
/* 隐藏滚动条并保留滚动功能 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.scrollbar-hide {
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}
</style>
