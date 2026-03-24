<script setup lang="ts">
import { Picture } from "@element-plus/icons-vue";
import type { FormInstance, FormRules } from "element-plus";
import { computed, ref, watch } from "vue";

import type { AdminAnime } from "@/types/adminAnime";

interface AnimeForm {
  title: {
    titleCn?: string;
    titleNative?: string;
    titleRomaji?: string;
    titleEn?: string;
  };
  coverImage?: string;
  startDate?: string;
}

const props = defineProps<{
  visible: boolean;
  anime?: AdminAnime; // 编辑模式时传入
}>();

const emit = defineEmits<{
  close: [];
  submit: [data: AnimeForm];
}>();

const formRef = ref<FormInstance>();
const formData = ref<AnimeForm>({
  title: {
    titleCn: "",
    titleNative: "",
    titleRomaji: "",
    titleEn: "",
  },
  coverImage: "",
  startDate: "",
});

// 是否为编辑模式
const isEditMode = computed(() => !!props.anime);

// 对话框标题
const dialogTitle = computed(() => (isEditMode.value ? "编辑动画" : "创建动画"));

// 表单验证规则
const rules: FormRules<AnimeForm> = {
  coverImage: [
    {
      type: "url",
      message: "请输入有效的图片URL",
      trigger: "blur",
      required: false,
    },
  ],
};

// 监听对话框打开，初始化表单数据
watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      if (props.anime) {
        // 编辑模式：填充现有数据
        formData.value = {
          title: {
            titleCn: props.anime.title.titleCn || "",
            titleNative: props.anime.title.titleNative || "",
            titleRomaji: props.anime.title.titleRomaji || "",
            titleEn: props.anime.title.titleEn || "",
          },
          coverImage: props.anime.coverImage || "",
          startDate: props.anime.startDate || "",
        };
      } else {
        // 创建模式：重置表单
        resetForm();
      }
    }
  },
);

// 重置表单
const resetForm = () => {
  formData.value = {
    title: {
      titleCn: "",
      titleNative: "",
      titleRomaji: "",
      titleEn: "",
    },
    coverImage: "",
    startDate: "",
  };
  formRef.value?.clearValidate();
};

// 关闭对话框
const handleClose = () => {
  emit("close");
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate((valid) => {
    if (valid) {
      emit("submit", formData.value);
    }
  });
};
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      label-position="left"
    >
      <el-divider content-position="left">标题信息</el-divider>

      <el-form-item label="中文标题" prop="title.titleCn">
        <el-input v-model="formData.title.titleCn" placeholder="请输入中文标题" clearable />
      </el-form-item>

      <el-form-item label="原生标题" prop="title.titleNative">
        <el-input v-model="formData.title.titleNative" placeholder="请输入原生标题" clearable />
      </el-form-item>

      <el-form-item label="罗马音标题" prop="title.titleRomaji">
        <el-input v-model="formData.title.titleRomaji" placeholder="请输入罗马音标题" clearable />
      </el-form-item>

      <el-form-item label="英文标题" prop="title.titleEn">
        <el-input v-model="formData.title.titleEn" placeholder="请输入英文标题" clearable />
      </el-form-item>

      <el-divider content-position="left">其他信息</el-divider>

      <el-form-item label="封面图片" prop="coverImage">
        <el-input v-model="formData.coverImage" placeholder="请输入封面图片 URL" clearable />
      </el-form-item>

      <el-form-item v-if="formData.coverImage" label="封面预览">
        <el-image
          :src="formData.coverImage"
          fit="cover"
          class="h-[170px] w-[120px] rounded"
          :preview-src-list="[formData.coverImage]"
        >
          <template #error>
            <div
              class="flex h-full w-full flex-col items-center justify-center bg-gray-100 text-xs text-gray-400"
            >
              <el-icon class="mb-1 text-2xl"><Picture /></el-icon>
              <span>加载失败</span>
            </div>
          </template>
        </el-image>
      </el-form-item>

      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
          v-model="formData.startDate"
          type="date"
          placeholder="请选择开始日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
:deep(.el-divider__text) {
  font-weight: 600;
  color: #1f2937;
}
</style>
