<script setup lang="ts">
import { Plus, Refresh } from "@element-plus/icons-vue";
import draggable from "vuedraggable";

import AdminMappingItem from "@/components/AdminMappingItem.vue";
import MappingFormDialog from "@/components/MappingFormDialog.vue";
import type { AdminMapping } from "@/types/adminAnime";

interface Props {
  mappingList: AdminMapping[];
  mappingDialogVisible: boolean;
  loading?: boolean;
  error?: string | null;
}

defineProps<Props>();

const emit = defineEmits<{
  "create-mapping": [];
  "delete-mapping": [mappingId: number];
  "mapping-change": [evt: any];
  "close-dialog": [];
  "submit-mapping": [sourcePlatform: string, platformId: string];
  refresh: [];
}>();
</script>

<template>
  <div class="flex h-full flex-col" v-loading="loading">
    <div class="mb-4 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <h2 class="m-0 text-xl font-semibold text-gray-800">未关联映射</h2>
        <el-tag type="warning">{{ mappingList.length }}</el-tag>
      </div>
      <div class="flex items-center gap-2">
        <el-button :icon="Refresh" circle size="small" @click="emit('refresh')" />
        <el-button type="primary" size="small" :icon="Plus" @click="emit('create-mapping')">
          新建映射
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="error"
      :title="error"
      type="error"
      center
      show-icon
      :closable="false"
      class="mb-4"
    />

    <div v-else class="flex-1 overflow-hidden rounded p-2 transition-colors">
      <draggable
        :model-value="mappingList"
        :group="{ name: 'mappings', pull: true, put: true }"
        item-key="mappingId"
        :sort="false"
        @change="emit('mapping-change', $event)"
        class="flex h-full flex-col gap-3 overflow-y-auto pr-2"
      >
        <template #item="{ element }">
          <AdminMappingItem :mapping="element" @delete-mapping="emit('delete-mapping', $event)" />
        </template>
      </draggable>
    </div>

    <!-- Mapping 表单对话框 -->
    <MappingFormDialog
      :visible="mappingDialogVisible"
      @close="emit('close-dialog')"
      @submit="(sourcePlatform, platformId) => emit('submit-mapping', sourcePlatform, platformId)"
    />
  </div>
</template>
