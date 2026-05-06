<script setup lang="ts">
import { getAllPlatformConfigs } from "@pjyk-web/shared/config/platforms.ts";
import { ElMessage } from "element-plus";
import { computed, ref } from "vue";

import { createMapping } from "@/api/mapping";
import type { AdminMapping } from "@/types/adminAnime";

const visible = defineModel<boolean>("visible", { required: true });
const mappingList = defineModel<AdminMapping[]>("mappingList", { required: true });

const formData = ref({
  sourcePlatform: "",
  platformId: "",
});

// 从平台配置中获取平台选项
const platformOptions = computed(() => {
  return getAllPlatformConfigs().map((config) => ({
    label: config.name,
    value: config.name,
  }));
});

const handleSubmit = async () => {
  if (!formData.value.sourcePlatform) {
    ElMessage.warning("请选择平台");
    return;
  }
  if (!formData.value.platformId.trim()) {
    ElMessage.warning("请输入平台 ID");
    return;
  }
  try {
    const newMapping = await createMapping(
      formData.value.sourcePlatform,
      formData.value.platformId.trim(),
    );
    mappingList.value.unshift(newMapping);
    ElMessage.success("映射创建成功");
    handleClose();
  } catch (e) {
    ElMessage.error("创建失败: " + (e instanceof Error ? e.message : "未知错误"));
  }
};

const handleClose = () => {
  formData.value = {
    sourcePlatform: "",
    platformId: "",
  };
  visible.value = false;
};
</script>

<template>
  <el-dialog :model-value="visible" title="创建新映射" width="500px" @close="handleClose">
    <el-form :model="formData" label-width="100px">
      <el-form-item label="平台" required>
        <el-select v-model="formData.sourcePlatform" placeholder="请选择平台" style="width: 100%">
          <el-option
            v-for="option in platformOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="平台 ID" required>
        <el-input v-model="formData.platformId" placeholder="请输入平台 ID" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">创建</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.list-disc {
  padding-left: 1rem;
}
</style>
