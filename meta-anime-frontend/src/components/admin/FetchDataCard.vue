<script setup lang="ts">
import { ref } from 'vue';
import { fetchAnime, fetchMapping, linkMappings, calculateScores } from '@/api/admin';
import { ElMessage } from 'element-plus';
import { Download, Refresh, SetUp } from '@element-plus/icons-vue';
import { SEASON_OPTIONS_REQUIRED } from '@/constants/ui-options';

// 数据抓取相关
const fetchDialogVisible = ref(false);
const fetchYear = ref(new Date().getFullYear());
const fetchSeason = ref<'SPRING' | 'SUMMER' | 'FALL' | 'WINTER'>('SPRING');
const fetchLoading = ref(false);

// 使用统一的季度选项（不含"全部"选项）
const seasonOptions = SEASON_OPTIONS_REQUIRED;

// 打开抓取对话框
const handleOpenFetchDialog = () => {
  fetchDialogVisible.value = true;
};

// 执行完整抓取流程
const handleFetchAnime = async () => {
  try {
    fetchLoading.value = true;
    await fetchAnime(fetchYear.value, fetchSeason.value);
    ElMessage.success('数据抓取任务已启动，请稍后查看结果');
    fetchDialogVisible.value = false;
  } catch (e) {
    ElMessage.error('启动失败: ' + (e instanceof Error ? e.message : '未知错误'));
  } finally {
    fetchLoading.value = false;
  }
};

// 只抓取映射
const handleFetchMapping = async () => {
  try {
    fetchLoading.value = true;
    await fetchMapping(fetchYear.value, fetchSeason.value);
    ElMessage.success('映射抓取任务已启动，请稍后查看结果');
    fetchDialogVisible.value = false;
  } catch (e) {
    ElMessage.error('启动失败: ' + (e instanceof Error ? e.message : '未知错误'));
  } finally {
    fetchLoading.value = false;
  }
};

// 合并映射
const handleLinkMappings = async () => {
  try {
    fetchLoading.value = true;
    await linkMappings();
    ElMessage.success('映射合并任务已启动，请稍后查看结果');
  } catch (e) {
    ElMessage.error('启动失败: ' + (e instanceof Error ? e.message : '未知错误'));
  } finally {
    fetchLoading.value = false;
  }
};

// 计算评分
const handleCalculateScores = async () => {
  try {
    fetchLoading.value = true;
    await calculateScores();
    ElMessage.success('评分计算任务已启动，请稍后查看结果');
  } catch (e) {
    ElMessage.error('启动失败: ' + (e instanceof Error ? e.message : '未知错误'));
  } finally {
    fetchLoading.value = false;
  }
};
</script>

<template>
  <div>
    <!-- 数据抓取卡片 -->
    <el-card class="hover:shadow-lg transition-shadow">
      <template #header>
        <div class="flex items-center gap-2">
          <el-icon><Download /></el-icon>
          <span class="font-semibold text-lg">数据抓取</span>
        </div>
      </template>
      <div class="">
        <p class="text-gray-600 mb-4">从外部平台抓取动画数据</p>
        <el-button
          type="primary"
          size="large"
          :icon="Download"
          @click="handleOpenFetchDialog"
        >
          抓取动画数据
        </el-button>
        <el-button
          size="large"
          :icon="Refresh"
          @click="handleLinkMappings"
          :loading="fetchLoading"
        >
          连接映射数据
        </el-button>
        <el-button
          size="large"
          :icon="SetUp"
          @click="handleCalculateScores"
          :loading="fetchLoading"
        >
          计算平均评分
        </el-button>
      </div>
    </el-card>

    <!-- 数据抓取对话框 -->
    <el-dialog
      v-model="fetchDialogVisible"
      title="抓取动画数据"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="年份">
          <el-input-number
            v-model="fetchYear"
            :min="2000"
            :max="2099"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="季度">
          <el-select
            v-model="fetchSeason"
            style="width: 100%"
          >
            <el-option
              v-for="option in seasonOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-alert
          title="完整抓取将依次执行：抓取映射 → 合并映射 → 计算评分"
          type="info"
          :closable="false"
          class="mb-4"
        />
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="fetchDialogVisible = false">取消</el-button>
          <el-button
            @click="handleFetchMapping"
            :loading="fetchLoading"
          >
            仅抓取映射
          </el-button>
          <el-button
            type="primary"
            @click="handleFetchAnime"
            :loading="fetchLoading"
          >
            完整抓取
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
