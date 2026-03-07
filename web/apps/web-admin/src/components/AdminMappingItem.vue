<script setup lang="ts">
import { Delete } from "@element-plus/icons-vue";
import { getPlatformConfig } from "@pjyk-web/shared/config/platforms.ts";
import { computed } from "vue";

import type { AdminMapping } from "@/types/adminAnime";

const props = defineProps<{
  mapping: AdminMapping;
}>();

const emit = defineEmits<{
  deleteMapping: [mappingId: number];
}>();

// 根据平台名称获取配置
const platformConfig = computed(() => getPlatformConfig(props.mapping.sourcePlatform));

// 生成动画条目URL
const animeUrl = computed(() => platformConfig.value.getAnimeUrl?.(props.mapping.platformId));

const titleTooltip = computed(() => {
  if (!props.mapping.mappingInfo) return "";
  const titleInfo = props.mapping.mappingInfo.title;
  let title = "";
  if (titleInfo.titleCn) {
    title += `titleCn: ${titleInfo.titleCn}\n`;
  }
  if (titleInfo.titleNative) {
    title += `titleNative: ${titleInfo.titleNative}\n`;
  }
  if (titleInfo.titleRomaji) {
    title += `titleRomaji: ${titleInfo.titleRomaji}\n`;
  }
  if (titleInfo.titleEn) {
    title += `titleEn: ${titleInfo.titleEn}\n`;
  }
  return title.trim();
});

const handleDelete = (e: Event) => {
  e.stopPropagation();
  emit("deleteMapping", props.mapping.mappingId);
};
</script>

<template>
  <div
    class="flex min-h-[50px] cursor-grab items-center gap-2.5 rounded-sm border border-gray-200 bg-white px-2.5 py-1.5 transition-all hover:border-gray-300 hover:bg-gray-50 hover:shadow-sm active:cursor-grabbing"
  >
    <el-image
      v-if="mapping.mappingInfo.coverImage"
      :src="mapping.mappingInfo.coverImage"
      :alt="mapping.mappingInfo.title.titleNative || mapping.mappingInfo.title.titleRomaji"
      class="h-12 w-8 shrink-0 rounded-sm object-cover"
      fit="cover"
      lazy
    />
    <div class="flex w-16 shrink-0 flex-col items-start py-0.5">
      <span class="mb-0.5 text-[10px] leading-tight text-gray-500">映射ID</span>
      <span
        class="w-full overflow-hidden text-xs font-medium text-ellipsis whitespace-nowrap text-gray-900"
      >
        {{ mapping.mappingId }}
      </span>
    </div>
    <div class="flex min-w-0 flex-1 flex-col items-start gap-0.5 py-1" :title="titleTooltip">
      <span
        class="w-full overflow-hidden text-sm font-medium text-ellipsis whitespace-nowrap text-gray-900"
      >
        {{ mapping.mappingInfo.title.titleCn || mapping.mappingInfo.title.titleNative }}
      </span>
      <span class="w-full overflow-hidden text-xs text-ellipsis whitespace-nowrap text-gray-500">
        {{ mapping.mappingInfo.title.titleNative }}
      </span>
    </div>
    <div class="flex w-8 shrink-0 flex-col items-start py-0.5">
      <span class="mb-0.5 text-[10px] leading-tight text-gray-500">评分</span>
      <span
        class="w-full overflow-hidden text-xs font-semibold text-ellipsis whitespace-nowrap text-amber-500"
      >
        {{ mapping.rawScore }}
      </span>
    </div>
    <a :href="animeUrl" target="_blank" rel="noopener noreferrer" @click.stop title="访问平台页面">
      <div class="flex w-[100px] shrink-0 justify-center py-0.5">
        <el-tag type="primary" size="small" effect="plain">{{ mapping.sourcePlatform }}</el-tag>
      </div>
    </a>
    <div
      v-if="mapping.mappingInfo.startDate"
      class="flex w-20 shrink-0 flex-col items-start py-0.5"
    >
      <span class="mb-0.5 text-[10px] leading-tight text-gray-500">开始日期</span>
      <span class="text-[11px] whitespace-nowrap text-gray-700">{{
        mapping.mappingInfo.startDate
      }}</span>
    </div>
    <div class="flex shrink-0 items-center">
      <el-button
        type="danger"
        size="small"
        :icon="Delete"
        circle
        @click="handleDelete"
        title="删除映射"
      />
    </div>
  </div>
</template>

<style scoped>
@media (max-width: 768px) {
  .flex.items-center {
    flex-wrap: wrap;
  }

  .flex.flex-col {
    width: auto !important;
  }
}
</style>
