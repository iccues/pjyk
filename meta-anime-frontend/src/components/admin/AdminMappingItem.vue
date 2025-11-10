<script setup lang="ts">
import type { AdminMapping } from '../../types/adminAnime';

defineProps<{
  mapping: AdminMapping;
}>();
</script>

<template>
  <div class="mapping-item-row">
    <div class="mapping-cell mapping-id-cell">
      <span class="cell-label">映射ID</span>
      <span class="cell-value">{{ mapping.mappingId }}</span>
    </div>
    <div class="mapping-cell mapping-platform-cell">
      <el-tag type="primary" size="small" effect="plain">{{ mapping.sourcePlatform }}</el-tag>
    </div>
    <div class="mapping-cell mapping-anime-id-cell">
      <span class="cell-label">动画ID</span>
      <span class="cell-value">{{ mapping.animeId }}</span>
    </div>
    <div class="mapping-cell mapping-platform-id-cell">
      <span class="cell-label">平台ID</span>
      <span class="cell-value mono">{{ mapping.platformId }}</span>
    </div>
    <div class="mapping-cell mapping-score-cell">
      <span class="cell-label">评分</span>
      <span class="cell-value score-value">{{ mapping.rawScore }}</span>
    </div>
  </div>
</template>

<style scoped>
.mapping-item-row {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  gap: 10px;
  background: white;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 3px;
  transition: all 0.2s;
  min-height: 50px;
  /* cursor: move; */
  cursor: grab;
}

.mapping-item-row:active {
  cursor: grabbing;
}

.mapping-item-row:hover {
  background: var(--el-fill-color-lighter);
  border-color: var(--el-border-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.mapping-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 2px 0;
}

.mapping-id-cell {
  flex-shrink: 0;
  width: 80px;
}

.mapping-platform-cell {
  flex-shrink: 0;
  width: 100px;
  justify-content: center;
}

.mapping-anime-id-cell {
  flex-shrink: 0;
  width: 80px;
}

.mapping-platform-id-cell {
  flex: 1;
  min-width: 0;
}

.mapping-score-cell {
  flex-shrink: 0;
  width: 70px;
}

.cell-label {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  line-height: 1.2;
  margin-bottom: 2px;
}

.cell-value {
  font-size: 12px;
  color: var(--el-text-color-primary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.cell-value.mono {
  font-family: monospace;
  font-size: 11px;
}

.score-value {
  color: var(--el-color-warning);
  font-weight: 600;
}

@media (max-width: 768px) {
  .mapping-item-row {
    flex-wrap: wrap;
  }

  .mapping-cell {
    width: auto !important;
  }
}
</style>
