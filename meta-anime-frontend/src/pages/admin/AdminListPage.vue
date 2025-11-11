<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getAnimeList, getUnmappedMappingList, updateMappingAnime, deleteAnime } from '../../api/admin';
import type { AdminAnime, AdminMapping } from '../../types/adminAnime';
import AdminAnimeItem from '../../components/admin/AdminAnimeItem.vue';
import AdminMappingItem from '../../components/admin/AdminMappingItem.vue';
import draggable from 'vuedraggable';
import { ElMessage, ElMessageBox } from 'element-plus';

const animeList = ref<AdminAnime[]>([]);
const mappingList = ref<AdminMapping[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    loading.value = true;
    const [animes, mappings] = await Promise.all([
      getAnimeList(),
      getUnmappedMappingList()
    ]);
    animeList.value = animes;
    mappingList.value = mappings;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载数据失败';
    console.error('Failed to load admin data:', e);
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
</script>

<template>
  <div class="admin-list-page">
    <el-page-header title="返回" content="管理后台" class="page-header" />

    <div v-loading="loading" class="content-wrapper">
      <el-alert
        v-if="error"
        :title="error"
        type="error"
        center
        show-icon
        :closable="false"
      />

      <el-row v-else :gutter="24" class="list-row">
        <!-- 左列：动画列表 -->
        <el-col :xs="24" :lg="12">
          <div class="list-section">
            <div class="section-header">
              <h2>动画列表</h2>
              <el-tag type="info">{{ animeList.length }}</el-tag>
            </div>
            <el-scrollbar height="calc(100vh - 200px)" class="list-scrollbar">
              <div class="list-content">
                <AdminAnimeItem
                  v-for="anime in animeList"
                  :key="anime.animeId"
                  :anime="anime"
                  @mapping-change="(evt) => handleMappingToAnime(evt, anime.animeId)"
                  @delete-anime="handleDeleteAnime"
                />
              </div>
            </el-scrollbar>
          </div>
        </el-col>

        <!-- 右列：未关联映射列表 -->
        <el-col :xs="24" :lg="12">
          <div class="list-section">
            <div class="section-header">
              <h2>未关联映射</h2>
              <el-tag type="warning">{{ mappingList.length }}</el-tag>
            </div>
            <el-scrollbar height="calc(100vh - 200px)" class="list-scrollbar">
              <div class="list-content drop-zone">
                <draggable
                  :model-value="mappingList"
                  :group="{ name: 'mappings', pull: true, put: true }"
                  item-key="mappingId"
                  :sort="false"
                  @change="handleMappingToUnmapped"
                >
                  <template #item="{ element }">
                    <AdminMappingItem :mapping="element" />
                  </template>
                </draggable>
              </div>
            </el-scrollbar>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.admin-list-page {
  padding: 20px;
  max-width: 1800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.content-wrapper {
  min-height: 400px;
}

.list-row {
  margin-top: 0;
}

.list-section {
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--el-border-color-lighter);
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
}

.list-scrollbar {
  flex: 1;
}

.list-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-right: 8px;
}

.drop-zone {
  min-height: 200px;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.drop-zone.sortable-drag {
  opacity: 0.5;
}

.drop-zone.sortable-ghost {
  opacity: 0.3;
  background: var(--el-color-primary-light-9);
}

.empty-unmapped {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: var(--el-text-color-placeholder);
  font-size: 14px;
  border: 2px dashed var(--el-border-color-lighter);
  border-radius: 4px;
  background: var(--el-fill-color-blank);
  margin: 8px;
}

@media (max-width: 992px) {
  .list-section {
    margin-bottom: 24px;
  }
}
</style>
