<script setup lang="ts">
import { Plus } from "@element-plus/icons-vue";
import draggable from "vuedraggable";
import AdminMappingItem from "@/components/AdminMappingItem.vue";
import MappingFormDialog from "@/components/MappingFormDialog.vue";
import type { AdminMapping } from "@/types/adminAnime";

interface Props {
  mappingList: AdminMapping[];
  mappingDialogVisible: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  "create-mapping": [];
  "delete-mapping": [mappingId: number];
  "mapping-change": [evt: any];
  "close-dialog": [];
  "submit-mapping": [sourcePlatform: string, platformId: string];
}>();
</script>

<template>
  <div class="flex flex-col h-full">
    <div class="flex justify-between items-center mb-4">
      <div class="flex items-center gap-3">
        <h2 class="text-xl font-semibold text-gray-800 m-0">未关联映射</h2>
        <el-tag type="warning">{{ mappingList.length }}</el-tag>
      </div>
      <el-button type="primary" size="small" :icon="Plus" @click="emit('create-mapping')">
        新建映射
      </el-button>
    </div>

    <div class="flex-1 p-2 rounded transition-colors overflow-hidden">
      <draggable
        :model-value="mappingList"
        :group="{ name: 'mappings', pull: true, put: true }"
        item-key="mappingId"
        :sort="false"
        @change="emit('mapping-change', $event)"
        class="h-full overflow-y-auto flex flex-col gap-3 pr-2"
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
