<script setup lang="ts">
import { Plus, Refresh } from "@element-plus/icons-vue";
import { useQuery } from "@tanstack/vue-query";
import { ElMessage, ElMessageBox } from "element-plus";
import { storeToRefs } from "pinia";
import { ref, toRaw, watch } from "vue";
import { VueDraggable } from "vue-draggable-plus";

import { deleteMapping, getUnmappedMappingList } from "@/api/mapping";
import AdminMappingItem from "@/components/AdminMappingItem.vue";
import MappingFormDialog from "@/components/MappingFormDialog.vue";
import { useAdminListPageStore } from "@/stores/adminListPageStore.ts";

// --- 1. Store 状态与核心逻辑 ---

const { mappingList } = storeToRefs(useAdminListPageStore());

const { applyMappingChange, removeMappingFromUnmapped } = useAdminListPageStore();

const mappingDialogVisible = ref(false);

// --- 2. 数据加载 (TanStack Query) ---

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

// --- 3. 组件本地 UI 操作 ---

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

    removeMappingFromUnmapped(mappingId);
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
      <VueDraggable
        :model-value="mappingList"
        :group="{ name: 'mappings', pull: true, put: true }"
        :sort="false"
        @add="applyMappingChange($event.data, null)"
        class="flex h-full flex-col gap-3 overflow-y-auto pr-2"
      >
        <AdminMappingItem
          v-for="mapping in mappingList"
          :key="mapping.mappingId"
          :mapping="mapping"
          @delete-mapping="handleDeleteMapping"
        />
      </VueDraggable>
    </div>

    <!-- Mapping 表单对话框 -->
    <MappingFormDialog v-model:visible="mappingDialogVisible" />
  </div>
</template>
