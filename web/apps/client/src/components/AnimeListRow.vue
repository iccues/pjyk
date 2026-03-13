<script setup lang="ts">
import { ArrowLeft, ArrowRight } from "@element-plus/icons-vue";
import { useQuery } from "@urql/vue";
import { computed, nextTick, ref, watch } from "vue";

import { GetAnimeListRowDocument, type Season, type SortBy } from "@/graphql/generated/graphql";
import { filtersToQuery } from "@/utils/queryUtils";

import AnimeCard from "./AnimeCard.vue";

const props = defineProps<{
  year?: number;
  season?: Season;
  sortBy?: SortBy;
  title?: string;
}>();

const scrollContainer = ref<HTMLElement | null>(null);
const showLeftButton = ref(false);
const showRightButton = ref(false);

// 构建查看更多链接的查询参数
const moreLink = computed(() => {
  const query = filtersToQuery(props);
  const queryString = new URLSearchParams(query).toString();
  return `/anime/list${queryString ? `?${queryString}` : ""}`;
});

const { data, fetching, error } = useQuery({
  query: GetAnimeListRowDocument,
  variables: computed(() => ({
    ...props,
    pageSize: 12,
  })),
});

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

watch(data, async () => {
  await nextTick();
  updateButtonVisibility();
});
</script>

<template>
  <!-- 标题栏 -->
  <div class="mx-auto mb-8 flex max-w-[1400px] items-center justify-between px-5">
    <h2 class="border-l-4 border-blue-500 pl-4 text-3xl font-bold text-gray-900">
      {{ title }}
    </h2>
    <router-link
      :to="moreLink"
      class="flex items-center gap-1 text-[14px] font-medium text-blue-600 transition-colors hover:text-blue-500"
    >
      查看更多 <span aria-hidden="true">&rarr;</span>
    </router-link>
  </div>

  <!-- 动画列表 -->
  <div v-if="error" class="py-10 text-center text-base text-red-600">{{ error }}</div>
  <div v-else-if="fetching" class="py-10 text-center text-base text-gray-600">加载中...</div>

  <div v-else-if="data && data?.animeList.content.length > 0" class="group/row relative">
    <!-- 滚动容器 -->
    <div
      ref="scrollContainer"
      @scroll="updateButtonVisibility"
      class="scrollbar-hide flex gap-5 overflow-x-auto scroll-smooth px-[max(1.25rem,calc(50%-700px+1.25rem))] pb-4"
    >
      <AnimeCard
        v-for="anime in data?.animeList?.content"
        :key="anime.animeId"
        :anime="anime"
        class="flex-shrink-0"
      />
    </div>

    <!-- 左侧滚动按钮 -->
    <button
      v-show="showLeftButton"
      @click="scrollLeft"
      class="absolute top-30 left-[max(0rem,calc(50%-700px))] z-10 flex h-12 w-12 items-center justify-center rounded-full bg-white/90 opacity-0 shadow-lg transition-opacity duration-300 group-hover/row:opacity-100 hover:scale-110 hover:bg-white active:scale-95"
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
      class="absolute top-30 right-[max(0rem,calc(50%-700px))] z-10 flex h-12 w-12 items-center justify-center rounded-full bg-white/90 opacity-0 shadow-lg transition-opacity duration-300 group-hover/row:opacity-100 hover:scale-110 hover:bg-white active:scale-95"
      aria-label="向右滚动"
    >
      <el-icon :size="24" class="text-gray-700">
        <ArrowRight />
      </el-icon>
    </button>
  </div>

  <div v-else class="py-10 text-center text-base text-gray-600">暂无数据</div>
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
