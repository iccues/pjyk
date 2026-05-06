<script setup lang="ts">
import { Plus, Refresh } from "@element-plus/icons-vue";
import { useQuery } from "@tanstack/vue-query";
import { ElMessage, ElMessageBox } from "element-plus";
import { ref, toRaw, watch } from "vue";
import draggable from "vuedraggable";

import { deleteMapping, getUnmappedMappingList } from "@/api/mapping";
import AdminMappingItem from "@/components/AdminMappingItem.vue";
import MappingFormDialog from "@/components/MappingFormDialog.vue";
import type { AdminMapping } from "@/types/adminAnime";

const emit = defineEmits<{
  "mapping-change": [evt: any];
}>();

// --- 1. 状态与绑定 ---

const mappingList = defineModel<AdminMapping[]>("mappingList", { required: true });

const mappingDialogVisible = ref(false);

// --- 2. 数据加载逻辑 ---

const { isFetching, isError, error, data, refetch } = useQuery({
  queryKey: ["admin-mapping-list"],
  queryFn: ({ signal }) => getUnmappedMappingList(signal),
});

watch(
  data,
  (newVal) => {
    if (newVal) mappingList.value = structuredClone(toRaw(newVal));
  },
  { immediate: true },
);

// --- 3. UI 操作处理器 ---

const handleDeleteMapping = async (mappingId: number) => {
  try {
    const mapping = mappingList.value.find((m) => m.mappingId === mappingId);
    if (!mapping) return;

    await ElMessageBox.confirm(`确定要删除该映射吗？此操作不可恢复。`, "确认删除", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    await deleteMapping(mappingId);

    const idx = mappingList.value.findIndex((m) => m.mappingId === mappingId);
    if (idx !== -1) mappingList.value.splice(idx, 1);

    ElMessage.success("删除成功");
  } catch (e) {
    if (e === "cancel") return;
    ElMessage.error("删除失败: " + (e instanceof Error ? e.message : "未知错误"));
  }
};
</script>

<template>
  <div class="flex h-full flex-col">
    <div class="mb-4 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <h2 class="m-0 text-xl font-semibold text-gray-800">未关联映射</h2>
        <el-tag type="warning">{{ mappingList.length }}</el-tag>
      </div>
      <div class="flex items-center gap-2">
        <el-button :icon="Refresh" circle size="small" @click="refetch" />
        <el-button type="primary" size="small" :icon="Plus" @click="mappingDialogVisible = true">
          新建映射
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="isError"
      v-loading="isFetching"
      :title="error?.message || '加载失败'"
      type="error"
      center
      show-icon
      :closable="false"
      class="mb-4"
    />

    <div v-else v-loading="isFetching" class="flex-1 overflow-hidden rounded p-2 transition-colors">
      <draggable
        :model-value="mappingList"
        :group="{ name: 'mappings', pull: true, put: true }"
        item-key="mappingId"
        :sort="false"
        @change="emit('mapping-change', $event)"
        class="flex h-full flex-col gap-3 overflow-y-auto pr-2"
      >
        <template #item="{ element }">
          <AdminMappingItem :mapping="element" @delete-mapping="handleDeleteMapping" />
        </template>
      </draggable>
    </div>

    <!-- Mapping 表单对话框 -->
    <MappingFormDialog v-model:visible="mappingDialogVisible" v-model:mappingList="mappingList" />
  </div>
</template>
