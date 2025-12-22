<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { VList } from 'virtua/vue'
import AdminAnimeItem from '@/components/admin/AdminAnimeItem.vue'
import AnimeFormDialog from '@/components/admin/AnimeFormDialog.vue'
import FilterBar from '@/components/admin/FilterBar.vue'
import type { AdminAnime, ReviewStatus } from '@/types/adminAnime'
import type { Season } from '@/types/anime'

interface Props {
  animeList: AdminAnime[]
  selectedReviewStatus?: ReviewStatus
  selectedYear?: number
  selectedSeason?: Season
  reviewStatusOptions: { label: string; value: ReviewStatus | undefined }[]
  yearOptions: { label: string; value: number | undefined }[]
  seasonOptions: { label: string; value: Season | undefined }[]
  dialogVisible: boolean
  editingAnime?: AdminAnime
}

defineProps<Props>()

const emit = defineEmits<{
  'update:selectedReviewStatus': [value: ReviewStatus | undefined]
  'update:selectedYear': [value: number | undefined]
  'update:selectedSeason': [value: Season | undefined]
  'filter-change': []
  'create-anime': []
  'edit-anime': [anime: AdminAnime]
  'delete-anime': [animeId: number]
  'update-review-status': [animeId: number, reviewStatus: ReviewStatus]
  'mapping-change': [evt: any, animeId: number]
  'close-dialog': []
  'submit-form': [formData: any]
}>()
</script>

<template>
  <div class="flex flex-col h-full">
    <div class="flex justify-between items-center mb-4">
      <div class="flex items-center gap-3">
        <h2 class="text-xl font-semibold text-gray-800 m-0">动画列表</h2>
        <el-tag type="info">{{ animeList.length }}</el-tag>
      </div>
      <el-button
        type="primary"
        size="small"
        :icon="Plus"
        @click="emit('create-anime')"
      >
        新建动画
      </el-button>
    </div>

    <FilterBar
      :selected-review-status="selectedReviewStatus"
      :selected-year="selectedYear"
      :selected-season="selectedSeason"
      :review-status-options="reviewStatusOptions"
      :year-options="yearOptions"
      :season-options="seasonOptions"
      @update:selected-review-status="emit('update:selectedReviewStatus', $event)"
      @update:selected-year="emit('update:selectedYear', $event)"
      @update:selected-season="emit('update:selectedSeason', $event)"
      @change="emit('filter-change')"
    />

    <VList
      :data="animeList"
      class="flex-1 pr-2 overflow-auto"
    >
      <template #default="{ item: anime }">
        <div :key="anime.animeId" class="mb-3">
          <AdminAnimeItem
            :anime="anime"
            @mapping-change="(evt) => emit('mapping-change', evt, anime.animeId)"
            @delete-anime="emit('delete-anime', $event)"
            @edit-anime="emit('edit-anime', $event)"
            @update-review-status="(animeId, reviewStatus) => emit('update-review-status', animeId, reviewStatus)"
          />
        </div>
      </template>
    </VList>

    <!-- 动画表单对话框 -->
    <AnimeFormDialog
      :visible="dialogVisible"
      :anime="editingAnime"
      @close="emit('close-dialog')"
      @submit="emit('submit-form', $event)"
    />
  </div>
</template>
