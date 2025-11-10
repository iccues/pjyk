<script setup lang="ts">
import { ref } from 'vue';
import { ArrowDown } from '@element-plus/icons-vue';
import type { AdminAnime } from '../../types/adminAnime';
import AdminMappingItem from './AdminMappingItem.vue';
import draggable from 'vuedraggable';

defineProps<{
  anime: AdminAnime;
}>();

const emit = defineEmits<{
  mappingChange: [evt: any];
}>();

const isExpanded = ref(false);

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value;
};

const handleMappingChange = (evt: any) => {
  emit('mappingChange', evt);
};
</script>

<template>
  <div class="anime-item-row" :class="{ expanded: isExpanded }">
    <div class="anime-row-content" @click="toggleExpand">
      <div class="anime-cell anime-cover-cell">
        <el-image
          :src="anime.coverImage"
          :alt="anime.title.titleCn || anime.title.titleNative"
          fit="cover"
          class="anime-cover"
        />
      </div>
      <div class="anime-cell anime-id-cell">
        <span class="cell-label">ID</span>
        <span class="cell-value">{{ anime.animeId }}</span>
      </div>
      <div class="anime-cell anime-title-cell">
        <div class="title-primary">{{ anime.title.titleCn || anime.title.titleNative }}</div>
        <div class="title-secondary">{{ anime.title.titleNative }}</div>
      </div>
      <div class="anime-cell anime-score-cell">
        <span class="cell-label">评分</span>
        <span class="cell-value score-value">{{ anime.averageScore?.toFixed(3) }}</span>
      </div>
      <div class="anime-cell anime-action-cell">
        <el-icon class="expand-icon" :class="{ expanded: isExpanded }">
          <ArrowDown />
        </el-icon>
      </div>
    </div>

    <el-collapse-transition>
      <div v-show="isExpanded" class="mappings-container" @click.stop>
        <draggable
          :model-value="anime.mappings"
          :group="{ name: 'mappings', pull: true, put: true }"
          item-key="mappingId"
          class="mappings-list"
          @change="handleMappingChange"
        >
          <template #item="{ element }">
            <AdminMappingItem :mapping="element" />
          </template>
        </draggable>
      </div>
    </el-collapse-transition>
  </div>
</template>

<style scoped>
.anime-item-row {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  background: white;
  transition: all 0.2s;
}

.anime-item-row:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-color: var(--el-border-color);
}

.anime-item-row.expanded {
  border-color: var(--el-color-primary-light-5);
}

.anime-row-content {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  gap: 12px;
  min-height: 60px;
}

.anime-row-content:hover {
  background: var(--el-fill-color-lighter);
}

.anime-cell {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.anime-cover-cell {
  flex-shrink: 0;
  width: 50px;
}

.anime-cover {
  width: 50px;
  height: 70px;
  border-radius: 2px;
}

.anime-id-cell {
  flex-shrink: 0;
  width: 80px;
  flex-direction: column;
  align-items: flex-start;
}

.anime-title-cell {
  flex: 1;
  min-width: 0;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.anime-score-cell {
  flex-shrink: 0;
  width: 80px;
  flex-direction: column;
  align-items: flex-start;
}

.anime-mappings-cell {
  flex-shrink: 0;
  width: 80px;
  flex-direction: column;
  align-items: flex-start;
}

.anime-action-cell {
  flex-shrink: 0;
  width: 40px;
  justify-content: center;
}

.cell-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  line-height: 1.2;
  margin-bottom: 2px;
}

.cell-value {
  font-size: 13px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.score-value {
  color: var(--el-color-success);
  font-weight: 600;
}

.title-primary {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.title-secondary {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.mapping-badge {
  margin-top: 2px;
}

.expand-icon {
  font-size: 14px;
  transition: transform 0.3s;
  color: var(--el-text-color-secondary);
}

.expand-icon.expanded {
  transform: rotate(180deg);
  color: var(--el-color-primary);
}

.mappings-container {
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-lighter);
  padding: 8px 12px 12px 12px;
}

.mappings-header {
  margin-bottom: 8px;
}

.mappings-title {
  font-size: 13px;
  color: var(--el-text-color-regular);
  font-weight: 600;
}

.mappings-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 60px;
}

.empty-mappings {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80px;
  color: var(--el-text-color-placeholder);
  font-size: 13px;
  border: 2px dashed var(--el-border-color-lighter);
  border-radius: 4px;
  background: var(--el-fill-color-blank);
}

@media (max-width: 768px) {
  .anime-row-content {
    flex-wrap: wrap;
  }

  .anime-title-cell {
    width: 100%;
    order: -1;
    margin-bottom: 8px;
  }
}
</style>
