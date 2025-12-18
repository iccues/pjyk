<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getAnimeList, getUnmappedMappingList, updateMappingAnime, deleteAnime, createAnime, updateAnime, createMapping, deleteMapping } from '@/api/admin';
import type { AdminAnime, AdminMapping, ReviewStatus } from '@/types/adminAnime';
import type { Season } from '@/types/anime';
import AdminAnimeItem from '@/components/admin/AdminAnimeItem.vue';
import AdminMappingItem from '@/components/admin/AdminMappingItem.vue';
import AnimeFormDialog from '@/components/admin/AnimeFormDialog.vue';
import MappingFormDialog from '@/components/admin/MappingFormDialog.vue';
import draggable from 'vuedraggable';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Filter, User } from '@element-plus/icons-vue';
import { VList } from 'virtua/vue';


const router = useRouter();

const animeList = ref<AdminAnime[]>([]);
const mappingList = ref<AdminMapping[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

// 筛选器状态
const selectedReviewStatus = ref<ReviewStatus | undefined>('PENDING');
const reviewStatusOptions = [
  { label: '全部', value: undefined },
  { label: '待审核', value: 'PENDING' as ReviewStatus },
  { label: '已通过', value: 'APPROVED' as ReviewStatus },
  { label: '已拒绝', value: 'REJECTED' as ReviewStatus }
];

const selectedYear = ref<number | undefined>(undefined);
const selectedSeason = ref<Season | undefined>(undefined);

const seasonOptions = [
  { label: '全部', value: undefined },
  { label: '冬季', value: 'WINTER' as Season },
  { label: '春季', value: 'SPRING' as Season },
  { label: '夏季', value: 'SUMMER' as Season },
  { label: '秋季', value: 'FALL' as Season }
];

// 生成年份选项（从当前年份往前推10年）
const currentYear = new Date().getFullYear();
const yearOptions = computed(() => {
  const years: { label: string; value: number | undefined }[] = [{ label: '全部', value: undefined }];
  for (let i = 0; i <= 10; i++) {
    const year = currentYear - i;
    years.push({ label: `${year}年`, value: year });
  }
  return years;
});

// 加载动画列表
const loadAnimeList = async () => {
  try {
    loading.value = true;
    const animes = await getAnimeList(
      selectedReviewStatus.value,
      selectedYear.value,
      selectedSeason.value
    );
    animeList.value = animes;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载动画列表失败';
  } finally {
    loading.value = false;
  }
};

// 处理筛选变化
const handleFilterChange = () => {
  loadAnimeList();
};

onMounted(async () => {
  try {
    loading.value = true;
    const [animes, mappings] = await Promise.all([
      getAnimeList(
        selectedReviewStatus.value,
        selectedYear.value,
        selectedSeason.value
      ),
      getUnmappedMappingList()
    ]);
    animeList.value = animes;
    mappingList.value = mappings;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载数据失败';
  } finally {
    loading.value = false;
  }
});

const applyMappingChange = async (mapping: AdminMapping, newAnimeId: number | null) => {
  const oldAnimeId = mapping.animeId
  if (oldAnimeId === newAnimeId) return

  try {
    await updateMappingAnime(mapping.mappingId, newAnimeId)

    if (oldAnimeId !== null) {
      const oldAnime = animeList.value.find(a => a.animeId === oldAnimeId)
      if (oldAnime) oldAnime.mappings = oldAnime.mappings.filter(m => m.mappingId !== mapping.mappingId)
    } else {
      const idx = mappingList.value.findIndex(m => m.mappingId === mapping.mappingId)
      if (idx !== -1) mappingList.value.splice(idx, 1)
    }

    mapping.animeId = newAnimeId

    if (newAnimeId !== null) {
      const newAnime = animeList.value.find(a => a.animeId === newAnimeId)
      if (newAnime && !newAnime.mappings.some(m => m.mappingId === mapping.mappingId)) {
        newAnime.mappings.push(mapping)
      }
      ElMessage.success('映射关联成功')
    } else {
      if (!mappingList.value.some(m => m.mappingId === mapping.mappingId)) {
        mappingList.value.push(mapping)
      }
      ElMessage.success('已解除映射关联')
    }
  } catch (e) {
    ElMessage.error('更新失败: ' + (e instanceof Error ? e.message : '未知错误'))
  }
}

const handleMappingToAnime = (evt: any, animeId: number) => {
  const mapping = evt.added?.element as AdminMapping | undefined
  if (mapping) applyMappingChange(mapping, animeId)
}

const handleMappingToUnmapped = (evt: any) => {
  const mapping = evt.added?.element as AdminMapping | undefined
  if (mapping) applyMappingChange(mapping, null)
}

const handleDeleteAnime = async (animeId: number) => {
  const anime = animeList.value.find(a => a.animeId === animeId)
  if (!anime) return

  try {
    const mappingCount = anime.mappings.length
    const confirmMessage = mappingCount > 0
      ? `确定要删除动画《${anime.title.titleCn || anime.title.titleNative}》吗？\n该动画包含 ${mappingCount} 个映射，将先解除关联后再删除动画。\n此操作不可恢复。`
      : `确定要删除动画《${anime.title.titleCn || anime.title.titleNative}》吗？此操作不可恢复。`

    await ElMessageBox.confirm(
      confirmMessage,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false,
      }
    )

    // 保存映射列表用于更新前端状态
    const mappingsToMove = [...anime.mappings]

    // 调用后端删除 API（后端会自动解除映射关联）
    await deleteAnime(animeId)

    // 更新前端状态：将映射移到未关联列表
    mappingsToMove.forEach(mapping => {
      mapping.animeId = null
      if (!mappingList.value.some(m => m.mappingId === mapping.mappingId)) {
        mappingList.value.push(mapping)
      }
    })

    // 从列表中移除该动画
    const idx = animeList.value.findIndex(a => a.animeId === animeId)
    if (idx !== -1) {
      animeList.value.splice(idx, 1)
    }

    ElMessage.success('删除成功')
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error('删除失败: ' + (e instanceof Error ? e.message : '未知错误'))
  }
}

// 动画表单对话框相关
const dialogVisible = ref(false)
const editingAnime = ref<AdminAnime | undefined>(undefined)

// 打开创建动画对话框
const handleCreateAnime = () => {
  editingAnime.value = undefined
  dialogVisible.value = true
}

// 打开编辑动画对话框
const handleEditAnime = (anime: AdminAnime) => {
  editingAnime.value = anime
  dialogVisible.value = true
}

// 关闭对话框
const handleCloseDialog = () => {
  dialogVisible.value = false
  editingAnime.value = undefined
}

// Mapping 表单对话框相关
const mappingDialogVisible = ref(false)

// 打开创建 Mapping 对话框
const handleCreateMapping = () => {
  mappingDialogVisible.value = true
}

// 关闭 Mapping 对话框
const handleCloseMappingDialog = () => {
  mappingDialogVisible.value = false
}

// 提交表单
const handleSubmitForm = async (formData: any) => {
  try {
    if (editingAnime.value) {
      // 编辑模式
      const updated = await updateAnime({
        animeId: editingAnime.value.animeId,
        ...formData
      })

      // 更新列表中的动画数据
      const idx = animeList.value.findIndex(a => a.animeId === editingAnime.value!.animeId)
      if (idx !== -1) {
        // 保留 mappings，只更新其他字段
        animeList.value[idx] = {
          ...updated,
          mappings: animeList.value[idx]?.mappings || []
        }
      }

      ElMessage.success('动画更新成功')
    } else {
      // 创建模式
      const created = await createAnime(formData)

      // 将新动画添加到列表顶部
      animeList.value.unshift(created)

      ElMessage.success('动画创建成功')
    }

    handleCloseDialog()
  } catch (e) {
    ElMessage.error('操作失败: ' + (e instanceof Error ? e.message : '未知错误'))
  }
}

// 处理审核状态更新
const handleUpdateReviewStatus = async (animeId: number, reviewStatus: ReviewStatus) => {
  try {
    const anime = animeList.value.find(a => a.animeId === animeId)
    if (!anime) return

    // 调用更新API
    const updated = await updateAnime({
      animeId,
      reviewStatus
    })

    // 检查更新后的状态是否符合当前筛选条件
    const idx = animeList.value.findIndex(a => a.animeId === animeId)
    if (idx !== -1) {
      if (selectedReviewStatus.value && reviewStatus !== selectedReviewStatus.value) {
        // 如果有筛选条件且新状态不符合，从列表中移除
        animeList.value.splice(idx, 1)
        ElMessage.success('审核状态已更新，该动画已从当前筛选列表中移除')
      } else {
        // 否则更新列表中的数据
        animeList.value[idx] = {
          ...updated,
          mappings: animeList.value[idx]?.mappings || []
        }
        ElMessage.success('审核状态更新成功')
      }
    }
  } catch (e) {
    ElMessage.error('更新失败: ' + (e instanceof Error ? e.message : '未知错误'))
  }
}

// 处理创建 Mapping
const handleSubmitMapping = async (sourcePlatform: string, platformId: string) => {
  try {
    const newMapping = await createMapping(sourcePlatform, platformId)
    mappingList.value.unshift(newMapping)
    ElMessage.success('映射创建成功')
    handleCloseMappingDialog()
  } catch (e) {
    ElMessage.error('创建失败: ' + (e instanceof Error ? e.message : '未知错误'))
  }
}

// 处理删除 Mapping
const handleDeleteMapping = async (mappingId: number) => {
  try {
    const mapping = mappingList.value.find(m => m.mappingId === mappingId)
    if (!mapping) return

    await ElMessageBox.confirm(
      `确定要删除该映射吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    await deleteMapping(mappingId)

    // 从列表中移除
    const idx = mappingList.value.findIndex(m => m.mappingId === mappingId)
    if (idx !== -1) {
      mappingList.value.splice(idx, 1)
    }

    ElMessage.success('删除成功')
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error('删除失败: ' + (e instanceof Error ? e.message : '未知错误'))
  }
}

// 导航到用户页面
const goToAuthHome = () => {
  router.push('/admin/auth');
};
</script>

<template>
  <div class="p-5 max-w-[1800px] mx-auto">
    <div class="flex justify-between items-center mb-6">
      <el-page-header title="返回" content="动画列表管理" @back="() => router.push('/admin')"/>
      <el-button :icon="User" circle @click="goToAuthHome" title="用户信息" />
    </div>

    <div v-loading="loading" class="min-h-[400px]">
      <el-alert
        v-if="error"
        :title="error"
        type="error"
        center
        show-icon
        :closable="false"
      />

      <el-row v-else :gutter="24">
        <!-- 左列：动画列表 -->
        <el-col :xs="24" :lg="12">
          <div class="flex flex-col">
            <div class="flex justify-between items-center mb-4 pb-3 border-b-2 border-gray-200">
              <div class="flex items-center gap-3">
                <h2 class="text-xl font-semibold text-gray-800 m-0">动画列表</h2>
                <el-tag type="info">{{ animeList.length }}</el-tag>
              </div>
              <el-button
                type="primary"
                size="small"
                :icon="Plus"
                @click="handleCreateAnime"
              >
                新建动画
              </el-button>
            </div>
            <!-- 筛选器 -->
            <div class="mb-3 flex flex-wrap items-center gap-2">
              <el-icon><Filter /></el-icon>
              <span class="text-sm text-gray-600">筛选：</span>

              <el-select
                v-model="selectedReviewStatus"
                placeholder="审核状态"
                size="small"
                style="width: 110px;"
                @change="handleFilterChange"
              >
                <el-option
                  v-for="option in reviewStatusOptions"
                  :key="option.label"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>

              <el-select
                v-model="selectedYear"
                placeholder="年份"
                size="small"
                style="width: 100px;"
                clearable
                @change="handleFilterChange"
              >
                <el-option
                  v-for="option in yearOptions"
                  :key="option.label"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>

              <el-select
                v-model="selectedSeason"
                placeholder="季度"
                size="small"
                style="width: 100px;"
                clearable
                @change="handleFilterChange"
              >
                <el-option
                  v-for="option in seasonOptions"
                  :key="option.label"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </div>
            <VList
              :data="animeList"
              style="height: calc(100vh - 240px);"
              class="pr-2"
            >
              <template #default="{ item: anime }">
                <div :key="anime.animeId" class="mb-3">
                  <AdminAnimeItem
                    :anime="anime"
                    @mapping-change="(evt) => handleMappingToAnime(evt, anime.animeId)"
                    @delete-anime="handleDeleteAnime"
                    @edit-anime="handleEditAnime"
                    @update-review-status="handleUpdateReviewStatus"
                  />
                </div>
              </template>
            </VList>
          </div>
        </el-col>

        <!-- 右列：未关联映射列表 -->
        <el-col :xs="24" :lg="12">
          <div class="flex flex-col">
            <div class="flex justify-between items-center mb-4 pb-3 border-b-2 border-gray-200">
              <div class="flex items-center gap-3">
                <h2 class="text-xl font-semibold text-gray-800 m-0">未关联映射</h2>
                <el-tag type="warning">{{ mappingList.length }}</el-tag>
              </div>
              <el-button
                type="primary"
                size="small"
                :icon="Plus"
                @click="handleCreateMapping"
              >
                新建映射
              </el-button>
            </div>
            <div class="min-h-[200px] p-2 rounded transition-colors" style="height: calc(100vh - 240px); overflow: hidden;">
              <draggable
                :model-value="mappingList"
                :group="{ name: 'mappings', pull: true, put: true }"
                item-key="mappingId"
                :sort="false"
                @change="handleMappingToUnmapped"
                style="height: 100%; overflow-y: auto;"
                class="flex flex-col gap-3 pr-2"
              >
                <template #item="{ element }">
                  <AdminMappingItem :mapping="element" @delete-mapping="handleDeleteMapping" />
                </template>
              </draggable>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 动画表单对话框 -->
    <AnimeFormDialog
      :visible="dialogVisible"
      :anime="editingAnime"
      @close="handleCloseDialog"
      @submit="handleSubmitForm"
    />

    <!-- Mapping 表单对话框 -->
    <MappingFormDialog
      :visible="mappingDialogVisible"
      @close="handleCloseMappingDialog"
      @submit="handleSubmitMapping"
    />
  </div>
</template>

<style scoped>
@media (max-width: 992px) {
  .flex.flex-col > .flex.justify-between:first-child {
    margin-bottom: 1.5rem;
  }
}
</style>
