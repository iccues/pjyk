<script setup lang="ts">
import { ArrowLeft, ArrowRight } from "@element-plus/icons-vue";
import { computed, onMounted, ref } from "vue";
import { getAnimeList } from "@/api/anime";
import type { Anime, Season, SortBy } from "@/types/anime.ts";
import type { Page } from "@/types/page.ts";
import { filtersToQuery } from "@/utils/queryUtils";
import AnimeCard from "./AnimeCard.vue";

const props = defineProps<{
  year?: number;
  season?: Season;
  sortBy?: SortBy;
  title?: string;
}>();

const animes = ref<Page<Anime> | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const scrollContainer = ref<HTMLElement | null>(null);
const showLeftButton = ref(false);
const showRightButton = ref(false);
const isHovering = ref(false);

// 构建查看更多链接的查询参数
const moreLink = computed(() => {
  const query = filtersToQuery(props);
  const queryString = new URLSearchParams(query).toString();
  return `/anime/list${queryString ? `?${queryString}` : ""}`;
});

const fetchAnimes = async () => {
  try {
    loading.value = true;
    error.value = null;
    animes.value = await getAnimeList({
      ...props,
      pageSize: 12,
    });
  } catch (err) {
    error.value = err instanceof Error ? err.message : "未知错误";
  } finally {
    loading.value = false;
  }
};

const updateButtonVisibility = () => {
  if (!scrollContainer.value) return;

  const { scrollLeft, scrollWidth, clientWidth } = scrollContainer.value;
  showLeftButton.value = scrollLeft > 0;
  showRightButton.value = scrollLeft < scrollWidth - clientWidth;
};

const getCardWidth = (): number => {
  // 默认值
  if (!scrollContainer.value) return 220;

  const firstCard = scrollContainer.value.querySelector(".flex-shrink-0") as HTMLElement;
  if (!firstCard) return 220;

  // 获取卡片宽度
  const cardWidth = firstCard.offsetWidth;

  // 获取 gap（通过比较第一个和第二个卡片的位置）
  const secondCard = firstCard.nextElementSibling as HTMLElement;
  if (secondCard) {
    const gap = secondCard.offsetLeft - (firstCard.offsetLeft + cardWidth);
    return cardWidth + gap;
  }

  // 如果只有一个卡片，使用默认 gap
  return cardWidth + 20;
};

const scrollLeft = () => {
  if (!scrollContainer.value) return;
  const totalCardWidth = getCardWidth();
  const index = Math.ceil(scrollContainer.value.scrollLeft / totalCardWidth);
  const targetScroll = (index - 2) * totalCardWidth;
  scrollContainer.value.scrollTo({
    left: targetScroll,
    behavior: "smooth",
  });
};

const scrollRight = () => {
  if (!scrollContainer.value) return;
  const totalCardWidth = getCardWidth();
  const index = Math.floor(scrollContainer.value.scrollLeft / totalCardWidth);
  const targetScroll = (index + 2) * totalCardWidth;
  scrollContainer.value.scrollTo({
    left: targetScroll,
    behavior: "smooth",
  });
};

onMounted(async () => {
  await fetchAnimes();
  updateButtonVisibility();
});
</script>

<template>
  <!-- 标题栏 -->
  <div class="flex max-w-[1400px] mx-auto px-5 justify-between items-center mb-8">
    <h2 class="text-3xl font-bold text-gray-900 border-l-4 border-blue-500 pl-4">
      {{ title }}
    </h2>
    <router-link
      :to="moreLink"
      class="text-[14px] font-medium text-blue-600 hover:text-blue-500 flex items-center gap-1 transition-colors"
    >
      查看更多 <span aria-hidden="true">&rarr;</span>
    </router-link>
  </div>

  <!-- 动画列表 -->
  <div v-if="error" class="text-center py-10 text-base text-red-600">{{ error }}</div>
  <div v-else-if="loading" class="text-center py-10 text-base text-gray-600">加载中...</div>

  <div
    v-else-if="animes && animes.content.length > 0"
    class="relative"
    @mouseenter="isHovering = true"
    @mouseleave="isHovering = false"
  >
    <!-- 滚动容器 -->
    <div
      ref="scrollContainer"
      @scroll="updateButtonVisibility"
      class="flex gap-5 overflow-x-auto pb-4 px-[max(1.25rem,calc(50%-700px+1.25rem))] scroll-smooth scrollbar-hide"
    >
      <AnimeCard
        v-for="anime in animes.content"
        :key="anime.animeId"
        :anime="anime"
        class="flex-shrink-0"
      />
    </div>

    <!-- 左侧滚动按钮 -->
    <button
      v-show="showLeftButton"
      @click="scrollLeft"
      class="absolute left-[max(0rem,calc(50%-700px))] top-30 z-10 w-12 h-12 bg-white/90 hover:bg-white shadow-lg rounded-full flex items-center justify-center transition-opacity duration-300 hover:scale-110 active:scale-95"
      :class="{ 'opacity-100': isHovering, 'opacity-0': !isHovering }"
      aria-label="向左滚动"
    >
      <el-icon :size="24" class="text-gray-700">
        <ArrowLeft />
      </el-icon>
    </button>

    <!-- 右侧滚动按钮 -->
    <button
      v-show="showRightButton"
      @click="scrollRight"
      class="absolute right-[max(0rem,calc(50%-700px))] top-30 z-10 w-12 h-12 bg-white/90 hover:bg-white shadow-lg rounded-full flex items-center justify-center transition-opacity duration-300 hover:scale-110 active:scale-95"
      :class="{ 'opacity-100': isHovering, 'opacity-0': !isHovering }"
      aria-label="向右滚动"
    >
      <el-icon :size="24" class="text-gray-700">
        <ArrowRight />
      </el-icon>
    </button>
  </div>

  <div v-else class="text-center py-10 text-base text-gray-600">暂无数据</div>
</template>

<style scoped>
/* 隐藏滚动条但保持滚动功能 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.scrollbar-hide {
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}
</style>
