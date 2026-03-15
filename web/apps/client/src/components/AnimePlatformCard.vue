<script setup lang="ts">
import { getPlatformConfig } from "@pjyk-web/shared/config/platforms.ts";
import { computed } from "vue";

import type { MappingItemFragment } from "@/graphql/generated/graphql";

const props = defineProps<{
  mapping: MappingItemFragment;
}>();

const platformConfig = computed(() => getPlatformConfig(props.mapping.sourcePlatform));
const animeUrl = computed(() => platformConfig.value.getAnimeUrl?.(props.mapping.platformId));
</script>

<template>
  <a
    v-if="animeUrl"
    :href="animeUrl"
    target="_blank"
    rel="noopener noreferrer"
    class="group flex flex-col gap-3 rounded-2xl border border-gray-100 bg-gray-50/50 p-5 no-underline transition-colors hover:bg-gray-100"
    @click.stop
  >
    <!-- Platform header -->
    <div class="flex items-center gap-3">
      <img
        v-if="platformConfig.logo"
        :src="platformConfig.logo"
        :alt="platformConfig.name"
        class="h-[24px] w-[24px] object-contain"
      />
      <span
        class="text-[15px] font-semibold text-gray-800 transition-colors group-hover:text-indigo-600"
      >
        {{ platformConfig.name }}
      </span>
    </div>

    <!-- Score rows -->
    <div class="grid grid-cols-2 gap-y-2 text-[14px]">
      <template v-if="mapping.rawScore != null">
        <span class="text-gray-500">原始评分</span>
        <span class="font-bold text-indigo-600">{{ mapping.rawScore.toFixed(1) }}</span>
      </template>
      <template v-if="mapping.normalizedScore != null">
        <span class="text-gray-500">标准化评分</span>
        <span class="font-bold text-blue-600">{{ mapping.normalizedScore.toFixed(2) }}</span>
      </template>
      <template v-if="mapping.normalizedPopularity != null">
        <span class="text-gray-500">标准化人气</span>
        <span class="font-medium text-gray-700">{{ mapping.normalizedPopularity.toFixed(2) }}</span>
      </template>
    </div>
  </a>
</template>
