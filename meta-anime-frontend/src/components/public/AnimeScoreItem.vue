<script setup lang="ts">
import { computed } from "vue";
import { getPlatformConfig } from "@/config/platforms";
import type { Mapping } from "@/types/anime";

const props = defineProps<{
  mapping: Mapping;
}>();

// 根据平台名称获取配置
const platformConfig = computed(() =>
  getPlatformConfig(props.mapping.sourcePlatform),
);

// 生成动画条目URL
const animeUrl = computed(() =>
  platformConfig.value.getAnimeUrl?.(props.mapping.platformId),
);
</script>

<template>
  <a v-if="animeUrl" :href="animeUrl" target="_blank" rel="noopener noreferrer"
    class="flex items-center justify-between px-3 py-2 bg-white rounded-full hover:opacity-80 transition-all cursor-pointer no-underline"
    @click.stop>
    <div class="flex items-center gap-2">
      <img v-if="platformConfig.logo" :src="platformConfig.logo" :alt="platformConfig.name"
        class="w-5 h-5 object-contain" />
      <span class="text-xs font-medium">{{ platformConfig.name }}</span>
    </div>
    <span class="text-sm font-bold text-blue-600">{{ mapping.rawScore?.toFixed(1) }}</span>
  </a>
  <div v-else class="flex items-center justify-between px-3 py-2 bg-white rounded-full opacity-75">
    <div class="flex items-center gap-2">
      <img v-if="platformConfig.logo" :src="platformConfig.logo" :alt="platformConfig.name"
        class="w-5 h-5 object-contain" />
      <span class="text-xs font-medium">{{ platformConfig.name }}</span>
    </div>
    <span class="text-sm font-bold text-blue-600">{{ mapping.rawScore?.toFixed(1) }}</span>
  </div>
</template>
