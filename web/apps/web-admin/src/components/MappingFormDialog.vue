<script setup lang="ts">
import { ElMessage } from "element-plus";
import { computed, ref, watch } from "vue";
import { getAllPlatformConfigs } from "@pjyk-web/shared/config/platforms.ts";

const props = defineProps<{
  visible: boolean;
}>();

const emit = defineEmits<{
  close: [];
  submit: [sourcePlatform: string, platformId: string];
}>();

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

// 当对话框关闭时重置表单
watch(
  () => props.visible,
  (newVal) => {
    if (!newVal) {
      formData.value = {
        sourcePlatform: "",
        platformId: "",
      };
    }
  },
);

const handleSubmit = () => {
  if (!formData.value.sourcePlatform) {
    ElMessage.warning("请选择平台");
    return;
  }
  if (!formData.value.platformId.trim()) {
    ElMessage.warning("请输入平台 ID");
    return;
  }
  emit("submit", formData.value.sourcePlatform, formData.value.platformId.trim());
};

const handleClose = () => {
  emit("close");
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
