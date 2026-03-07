<script setup lang="ts">
import { getPlatformConfig } from "@pjyk-web/shared/config/platforms.ts";
import { computed } from "vue";

import type { ScoreItemFragment } from "@/graphql/generated/graphql";

const props = defineProps<{
  mapping: ScoreItemFragment;
}>();

// 根据平台名称获取配置
const platformConfig = computed(() => getPlatformConfig(props.mapping.sourcePlatform));

// 生成动画条目URL
const animeUrl = computed(() => platformConfig.value.getAnimeUrl?.(props.mapping.platformId));
</script>

<template>
  <a
    v-if="animeUrl"
    :href="animeUrl"
    target="_blank"
    rel="noopener noreferrer"
    class="flex cursor-pointer items-center justify-between rounded-full bg-white px-3 py-2 no-underline transition-all hover:opacity-80"
    @click.stop
  >
    <div class="flex items-center gap-2">
      <img
        v-if="platformConfig.logo"
        :src="platformConfig.logo"
        :alt="platformConfig.name"
        class="h-5 w-5 object-contain"
      />
      <span class="text-xs font-medium">{{ platformConfig.name }}</span>
    </div>
    <span class="text-sm font-bold text-blue-600">{{ mapping.rawScore?.toFixed(1) }}</span>
  </a>
  <div v-else class="flex items-center justify-between rounded-full bg-white px-3 py-2 opacity-75">
    <div class="flex items-center gap-2">
      <img
        v-if="platformConfig.logo"
        :src="platformConfig.logo"
        :alt="platformConfig.name"
        class="h-5 w-5 object-contain"
      />
      <span class="text-xs font-medium">{{ platformConfig.name }}</span>
    </div>
    <span class="text-sm font-bold text-blue-600">{{ mapping.rawScore?.toFixed(1) }}</span>
  </div>
</template>
