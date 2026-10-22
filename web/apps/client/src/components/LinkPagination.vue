<script setup lang="ts">
import { ArrowLeft, ArrowRight, DArrowLeft, DArrowRight } from "@element-plus/icons-vue";
import { computed } from "vue";
import type { RouteLocationRaw } from "vue-router";

import LinkButton from "./LinkButton.vue";

const props = defineProps<{
  /** 当前页和 pageLink 参数均从 0 开始 */
  currentPage: number;
  pageCount: number;
  total: number;
  pageLink: (pageNumber: number) => RouteLocationRaw;
}>();

const pageItems = computed<(number | null)[]>(() => {
  const { currentPage, pageCount } = props;
  const lastPage = pageCount - 1;

  if (pageCount <= 9) {
    return Array.from({ length: pageCount }, (_, index) => index);
  }

  if (currentPage <= 4) {
    return [0, 1, 2, 3, 4, 5, 6, null, lastPage];
  }

  if (currentPage >= pageCount - 5) {
    return [
      0,
      null,
      lastPage - 6,
      lastPage - 5,
      lastPage - 4,
      lastPage - 3,
      lastPage - 2,
      lastPage - 1,
      lastPage,
    ];
  }

  return [
    0,
    null,
    currentPage - 2,
    currentPage - 1,
    currentPage,
    currentPage + 1,
    currentPage + 2,
    null,
    lastPage,
  ];
});

// 页码和省略号使用相同尺寸，窄屏总宽度保持在 320px 以内。
const cellClass = "inline-flex size-9 items-center justify-center rounded-[10px] text-[14px]";
</script>

<template>
  <div class="flex flex-col items-center gap-2">
    <template v-if="pageCount > 1">
      <!-- 窄屏显示当前页、总页数和首尾跳转。 -->
      <nav class="flex items-center justify-center gap-1 sm:hidden" aria-label="分页导航">
        <LinkButton
          :to="pageLink(0)"
          :disabled="currentPage <= 0"
          :class="cellClass"
          aria-label="第一页"
        >
          <DArrowLeft class="size-4.5" />
        </LinkButton>

        <LinkButton
          :to="pageLink(Math.max(currentPage - 1, 0))"
          :disabled="currentPage <= 0"
          :class="cellClass"
          aria-label="上一页"
        >
          <ArrowLeft class="size-4.5" />
        </LinkButton>

        <LinkButton
          :to="pageLink(currentPage)"
          active
          :class="cellClass"
          :aria-label="`第 ${currentPage + 1} 页`"
        >
          {{ currentPage + 1 }}
        </LinkButton>

        <span :class="[cellClass, 'text-gray-400']" aria-hidden="true">/</span>

        <span :class="[cellClass, 'text-gray-600']">{{ pageCount }}</span>

        <LinkButton
          :to="pageLink(Math.min(currentPage + 1, pageCount - 1))"
          :disabled="currentPage >= pageCount - 1"
          :class="cellClass"
          aria-label="下一页"
        >
          <ArrowRight class="size-4.5" />
        </LinkButton>

        <LinkButton
          :to="pageLink(pageCount - 1)"
          :disabled="currentPage >= pageCount - 1"
          :class="cellClass"
          aria-label="最后一页"
        >
          <DArrowRight class="size-4.5" />
        </LinkButton>
      </nav>

      <!-- 宽屏显示完整页码。 -->
      <nav class="hidden items-center gap-1 sm:flex" aria-label="分页导航">
        <LinkButton
          :to="pageLink(Math.max(currentPage - 1, 0))"
          :disabled="currentPage <= 0"
          :class="cellClass"
          aria-label="上一页"
        >
          <ArrowLeft class="size-4.5" />
        </LinkButton>

        <template v-for="(item, index) in pageItems" :key="item ?? `ellipsis-${index}`">
          <span v-if="item === null" :class="[cellClass, 'text-gray-400']" aria-hidden="true">
            …
          </span>
          <LinkButton
            v-else
            :to="pageLink(item)"
            :active="item === currentPage"
            :aria-label="`第 ${item + 1} 页`"
            :class="cellClass"
          >
            {{ item + 1 }}
          </LinkButton>
        </template>

        <LinkButton
          :to="pageLink(Math.min(currentPage + 1, pageCount - 1))"
          :disabled="currentPage >= pageCount - 1"
          :class="cellClass"
          aria-label="下一页"
        >
          <ArrowRight class="size-4.5" />
        </LinkButton>
      </nav>
    </template>

    <span class="text-[14px] text-gray-500">共 {{ total }} 部</span>
  </div>
</template>
