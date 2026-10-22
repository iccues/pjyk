<script setup lang="ts">
import { TopRight } from "@element-plus/icons-vue";
import { getPlatformConfig } from "@pjyk-web/shared/config/platforms.ts";
import { computed } from "vue";

import type { MappingItemFragment } from "@/graphql/generated/graphql";

const props = defineProps<{
  mapping: MappingItemFragment;
}>();

const platformConfig = computed(() => getPlatformConfig(props.mapping.sourcePlatform));
const animeUrl = computed(() => platformConfig.value.getAnimeUrl?.(props.mapping.platformId));
const formater = new Intl.NumberFormat("en-US", { maximumFractionDigits: 0 });
</script>

<template>
  <a
    v-if="animeUrl"
    :href="animeUrl"
    target="_blank"
    rel="noopener noreferrer"
    class="group flex flex-col gap-3 rounded-2xl border border-gray-100 bg-gray-50/50 p-5 no-underline transition-colors hover:bg-gray-100"
  >
    <div class="flex items-center gap-3">
      <img
        v-if="platformConfig.logo"
        :src="platformConfig.logo"
        :alt="platformConfig.name"
        class="size-6 object-contain"
      />
      <span
        class="text-[15px] font-semibold text-gray-800 transition-colors group-hover:text-indigo-600"
      >
        {{ platformConfig.name }}
      </span>
      <!-- 通过父元素设置图标颜色，并加粗描边。 -->
      <span
        aria-hidden="true"
        class="ml-auto inline-flex shrink-0 text-gray-400 transition-colors group-hover:text-gray-600 [&_svg]:stroke-current [&_svg]:stroke-60"
      >
        <TopRight class="size-3.5" />
      </span>
    </div>

    <!-- 标准化评分 -->
    <div v-if="mapping.normalizedScore != null" class="flex items-baseline gap-2">
      <span class="text-[22px] leading-none font-bold text-indigo-600 tabular-nums">
        {{ mapping.normalizedScore.toFixed(1) }}
      </span>
      <span class="text-[13px] text-gray-500">标准化评分</span>
    </div>

    <!-- 补充评分与人气数据；无内容时隐藏。 -->
    <dl class="flex flex-wrap gap-x-6 gap-y-2 empty:hidden">
      <div v-if="mapping.rawScore != null" class="flex flex-col gap-0.5">
        <dt class="text-[12px] text-gray-500">原始评分</dt>
        <dd class="text-[14px] font-semibold text-indigo-600 tabular-nums">
          {{ mapping.rawScore.toFixed(1) }}
        </dd>
      </div>
      <div v-if="mapping.rawPopularity != null" class="flex flex-col gap-0.5">
        <dt class="text-[12px] text-gray-500">原始人气</dt>
        <dd class="text-[14px] font-semibold text-pink-500 tabular-nums">
          {{ formater.format(mapping.rawPopularity) }}
        </dd>
      </div>
      <div v-if="mapping.normalizedPopularity != null" class="flex flex-col gap-0.5">
        <dt class="text-[12px] text-gray-500">标准化人气</dt>
        <dd class="text-[14px] font-semibold text-pink-500 tabular-nums">
          {{ formater.format(mapping.normalizedPopularity) }}
        </dd>
      </div>
    </dl>
  </a>
</template>
