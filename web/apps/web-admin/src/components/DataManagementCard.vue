<script setup lang="ts">
import { Delete } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { ref } from "vue";

import { deleteNonApprovedAnimes } from "@/api/admin";

const loading = ref(false);

// 删除所有未通过的动画
const handleDeleteNonApprovedAnimes = async () => {
  try {
    await ElMessageBox.confirm(
      "此操作将永久删除所有 PENDING 和 REJECTED 状态的动画及其关联的映射数据，是否继续？",
      "警告",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning",
        confirmButtonClass: "el-button--danger",
      },
    );

    loading.value = true;
    await deleteNonApprovedAnimes();
    ElMessage.success("已成功删除所有未通过的动画");
  } catch (e) {
    if (e === "cancel") {
      // 用户取消操作
      return;
    }
    ElMessage.error("删除失败: " + (e instanceof Error ? e.message : "未知错误"));
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <el-card class="transition-shadow hover:shadow-lg">
    <template #header>
      <div class="flex items-center gap-2">
        <el-icon><Delete /></el-icon>
        <span class="text-lg font-semibold">数据管理</span>
      </div>
    </template>
    <div>
      <p class="mb-4 text-gray-600">管理和清理动画数据</p>
      <el-button
        type="danger"
        size="large"
        :icon="Delete"
        @click="handleDeleteNonApprovedAnimes"
        :loading="loading"
      >
        删除未通过动画
      </el-button>
      <p class="mt-3 text-sm text-gray-500">删除所有 PENDING 和 REJECTED 状态的动画及其映射</p>
    </div>
  </el-card>
</template>
