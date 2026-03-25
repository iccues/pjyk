<script setup lang="ts">
import { Download, Refresh, SetUp } from "@element-plus/icons-vue";
import { PLATFORM_OPTIONS, SEASON_OPTIONS } from "@pjyk-web/shared/constants/ui-options.ts";
import { ElMessage } from "element-plus";
import { ref } from "vue";

import { calculateMetric, fetchAnime, fetchMapping, linkMappings } from "@/api/admin";

// 数据抓取相关
const fetchDialogVisible = ref(false);
const fetchYear = ref(new Date().getFullYear());
const fetchSeason = ref<string | undefined>(undefined);
const fetchPlatform = ref<string | undefined>(undefined);
const fetchLoading = ref(false);

// 使用含"全部"选项的季度选项
const seasonOptions = SEASON_OPTIONS;
const platformOptions = PLATFORM_OPTIONS;

// 打开抓取对话框
const handleOpenFetchDialog = () => {
  fetchDialogVisible.value = true;
};

// 执行完整抓取流程
const handleFetchAnime = async () => {
  try {
    fetchLoading.value = true;
    await fetchAnime(fetchYear.value, fetchSeason.value, fetchPlatform.value);
    ElMessage.success("数据抓取任务已启动，请稍后查看结果");
    fetchDialogVisible.value = false;
  } catch (e) {
    ElMessage.error("启动失败: " + (e instanceof Error ? e.message : "未知错误"));
  } finally {
    fetchLoading.value = false;
  }
};

// 只抓取映射
const handleFetchMapping = async () => {
  try {
    fetchLoading.value = true;
    await fetchMapping(fetchYear.value, fetchSeason.value, fetchPlatform.value);
    ElMessage.success("映射抓取任务已启动，请稍后查看结果");
    fetchDialogVisible.value = false;
  } catch (e) {
    ElMessage.error("启动失败: " + (e instanceof Error ? e.message : "未知错误"));
  } finally {
    fetchLoading.value = false;
  }
};

// 合并映射
const handleLinkMappings = async () => {
  try {
    fetchLoading.value = true;
    await linkMappings();
    ElMessage.success("映射合并任务已启动，请稍后查看结果");
  } catch (e) {
    ElMessage.error("启动失败: " + (e instanceof Error ? e.message : "未知错误"));
  } finally {
    fetchLoading.value = false;
  }
};

// 计算动漫指标
const handleCalculateScores = async () => {
  try {
    fetchLoading.value = true;
    await calculateMetric();
    ElMessage.success("指标计算任务已启动，请稍后查看结果");
  } catch (e) {
    ElMessage.error("启动失败: " + (e instanceof Error ? e.message : "未知错误"));
  } finally {
    fetchLoading.value = false;
  }
};
</script>

<template>
  <div>
    <!-- 数据抓取卡片 -->
    <el-card class="transition-shadow hover:shadow-lg">
      <template #header>
        <div class="flex items-center gap-2">
          <el-icon><Download /></el-icon>
          <span class="text-lg font-semibold">数据抓取</span>
        </div>
      </template>
      <div class="">
        <p class="mb-4 text-gray-600">从外部平台抓取动画数据</p>
        <el-button type="primary" size="large" :icon="Download" @click="handleOpenFetchDialog">
          抓取动画数据
        </el-button>
        <el-button size="large" :icon="Refresh" @click="handleLinkMappings" :loading="fetchLoading">
          连接映射数据
        </el-button>
        <el-button
          size="large"
          :icon="SetUp"
          @click="handleCalculateScores"
          :loading="fetchLoading"
        >
          计算动漫指标
        </el-button>
      </div>
    </el-card>

    <!-- 数据抓取对话框 -->
    <el-dialog v-model="fetchDialogVisible" title="抓取动画数据" width="500px">
      <el-form label-width="80px">
        <el-form-item label="年份">
          <el-input-number v-model="fetchYear" :min="2000" :max="2099" style="width: 100%" />
        </el-form-item>
        <el-form-item label="季度">
          <el-select
            v-model="fetchSeason"
            style="width: 100%"
            placeholder="选择季度（留空则抓取全年）"
          >
            <el-option
              v-for="option in seasonOptions"
              :key="option.value ?? 'ALL'"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="平台">
          <el-select
            v-model="fetchPlatform"
            style="width: 100%"
            placeholder="选择平台（留空则抓取全部）"
          >
            <el-option
              v-for="option in platformOptions"
              :key="option.value ?? 'ALL'"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-alert
          title="完整抓取将依次执行：抓取映射 → 合并映射 → 计算指标"
          type="info"
          :closable="false"
          class="mb-4"
        />
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="fetchDialogVisible = false">取消</el-button>
          <el-button @click="handleFetchMapping" :loading="fetchLoading"> 仅抓取映射 </el-button>
          <el-button type="primary" @click="handleFetchAnime" :loading="fetchLoading">
            完整抓取
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
