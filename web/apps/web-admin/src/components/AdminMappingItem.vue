<script setup lang="ts">
import { Delete } from "@element-plus/icons-vue";
import { computed } from "vue";
import { getPlatformConfig } from "@pjyk-web/shared/config/platforms.ts";
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
    class="flex items-center py-1.5 px-2.5 gap-2.5 bg-white border border-gray-200 rounded-sm transition-all min-h-[50px] cursor-grab hover:bg-gray-50 hover:border-gray-300 hover:shadow-sm active:cursor-grabbing"
  >
    <el-image
      v-if="mapping.mappingInfo.coverImage"
      :src="mapping.mappingInfo.coverImage"
      :alt="mapping.mappingInfo.title.titleNative || mapping.mappingInfo.title.titleRomaji"
      class="shrink-0 w-8 h-12 object-cover rounded-sm"
      fit="cover"
      lazy
    />
    <div class="shrink-0 w-16 flex flex-col items-start py-0.5">
      <span class="text-[10px] text-gray-500 leading-tight mb-0.5">映射ID</span>
      <span
        class="text-xs text-gray-900 font-medium whitespace-nowrap overflow-hidden text-ellipsis w-full"
      >
        {{ mapping.mappingId }}
      </span>
    </div>
    <div class="flex-1 min-w-0 flex flex-col items-start gap-0.5 py-1" :title="titleTooltip">
      <span
        class="text-sm font-medium text-gray-900 whitespace-nowrap overflow-hidden text-ellipsis w-full"
      >
        {{ mapping.mappingInfo.title.titleCn || mapping.mappingInfo.title.titleNative }}
      </span>
      <span class="text-xs text-gray-500 whitespace-nowrap overflow-hidden text-ellipsis w-full">
        {{ mapping.mappingInfo.title.titleNative }}
      </span>
    </div>
    <div class="shrink-0 w-8 flex flex-col items-start py-0.5">
      <span class="text-[10px] text-gray-500 leading-tight mb-0.5">评分</span>
      <span
        class="text-xs text-amber-500 font-semibold whitespace-nowrap overflow-hidden text-ellipsis w-full"
      >
        {{ mapping.rawScore }}
      </span>
    </div>
    <a :href="animeUrl" target="_blank" rel="noopener noreferrer" @click.stop title="访问平台页面">
      <div class="shrink-0 w-[100px] flex justify-center py-0.5">
        <el-tag type="primary" size="small" effect="plain">{{ mapping.sourcePlatform }}</el-tag>
      </div>
    </a>
    <div
      v-if="mapping.mappingInfo.startDate"
      class="shrink-0 w-20 flex flex-col items-start py-0.5"
    >
      <span class="text-[10px] text-gray-500 leading-tight mb-0.5">开始日期</span>
      <span class="text-[11px] text-gray-700 whitespace-nowrap">{{
        mapping.mappingInfo.startDate
      }}</span>
    </div>
    <div class="shrink-0 flex items-center">
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
