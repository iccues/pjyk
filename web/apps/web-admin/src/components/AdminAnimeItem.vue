<script setup lang="ts">
import { ArrowDown, Delete, Edit } from "@element-plus/icons-vue";
import { ref } from "vue";
import draggable from "vuedraggable";

import type { AdminAnime, ReviewStatus } from "@/types/adminAnime";

import AdminMappingItem from "./AdminMappingItem.vue";

const props = defineProps<{
  anime: AdminAnime;
}>();

const emit = defineEmits<{
  mappingChange: [evt: any];
  deleteAnime: [animeId: number];
  editAnime: [anime: AdminAnime];
  updateReviewStatus: [animeId: number, reviewStatus: ReviewStatus];
}>();

const isExpanded = ref(false);

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value;
};

const handleMappingChange = (evt: any) => {
  emit("mappingChange", evt);
};

const handleDelete = (event: Event) => {
  event.stopPropagation();
  emit("deleteAnime", props.anime.animeId);
};

const handleEdit = (event: Event) => {
  event.stopPropagation();
  emit("editAnime", props.anime);
};

// 审核状态选项
const reviewStatusOptions = [
  { label: "待审核", value: "PENDING" as ReviewStatus },
  { label: "已通过", value: "APPROVED" as ReviewStatus },
  { label: "已拒绝", value: "REJECTED" as ReviewStatus },
];

// 处理审核状态变化
const handleReviewStatusChange = (newStatus: ReviewStatus) => {
  if (newStatus !== props.anime.reviewStatus) {
    emit("updateReviewStatus", props.anime.animeId, newStatus);
  }
};
</script>

<template>
  <div
    class="rounded border border-gray-200 bg-white transition-all"
    :class="{ 'border-blue-300': isExpanded, 'hover:border-gray-300 hover:shadow-md': true }"
  >
    <div
      class="flex min-h-[60px] cursor-pointer items-center gap-3 px-3 py-2 hover:bg-gray-50"
      @click="toggleExpand"
    >
      <div class="w-[50px] shrink-0">
        <el-image
          :src="anime.coverImage"
          :alt="anime.title.titleCn || anime.title.titleNative"
          fit="cover"
          class="h-[70px] w-[50px] rounded-sm"
          lazy
        />
      </div>
      <div class="flex w-12 shrink-0 flex-col items-start py-1">
        <span class="mb-0.5 text-[10px] leading-tight text-gray-500">ID</span>
        <span class="text-xs font-medium text-gray-900">{{ anime.animeId }}</span>
      </div>
      <div class="flex min-w-0 flex-1 flex-col items-start gap-0.5 py-1">
        <div
          class="w-full overflow-hidden text-sm font-semibold text-ellipsis whitespace-nowrap text-gray-900"
        >
          {{ anime.title.titleCn || anime.title.titleNative }}
        </div>
        <div class="w-full overflow-hidden text-xs text-ellipsis whitespace-nowrap text-gray-500">
          {{ anime.title.titleNative }}
        </div>
      </div>
      <div class="flex w-24 shrink-0 flex-col items-center py-1" @click.stop>
        <span class="mb-0.5 text-[10px] leading-tight text-gray-500">审核</span>
        <el-select
          :model-value="anime.reviewStatus"
          size="small"
          @change="handleReviewStatusChange"
          style="width: 80px"
        >
          <el-option
            v-for="option in reviewStatusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>
      <div class="flex w-[100px] shrink-0 items-center justify-center gap-1">
        <el-button
          type="primary"
          size="small"
          :icon="Edit"
          circle
          @click="handleEdit"
          title="编辑动画"
        />
        <el-button
          type="danger"
          size="small"
          :icon="Delete"
          circle
          @click="handleDelete"
          title="删除动画"
        />
        <el-icon
          class="text-sm transition-transform duration-300"
          :class="isExpanded ? 'rotate-180 text-blue-500' : 'text-gray-500'"
        >
          <ArrowDown />
        </el-icon>
      </div>
    </div>

    <el-collapse-transition>
      <div v-if="isExpanded" class="border-t border-gray-200 bg-gray-50 px-3 py-2 pb-3" @click.stop>
        <draggable
          :model-value="anime.mappings"
          :group="{ name: 'mappings', pull: true, put: true }"
          item-key="mappingId"
          class="flex min-h-[60px] flex-col gap-1.5"
          @change="handleMappingChange"
        >
          <template #item="{ element }">
            <AdminMappingItem :mapping="element" />
          </template>
        </draggable>
      </div>
    </el-collapse-transition>
  </div>
</template>

<style scoped>
@media (max-width: 768px) {
  .flex.items-center.py-2 {
    flex-wrap: wrap;
  }

  .flex-1.min-w-0 {
    width: 100%;
    order: -1;
    margin-bottom: 0.5rem;
  }
}
</style>
