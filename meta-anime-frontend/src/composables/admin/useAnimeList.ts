import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnimeList, deleteAnime, createAnime, updateAnime } from '@/api/admin'
import type { AdminAnime, ReviewStatus } from '@/types/adminAnime'
import type { Season } from '@/types/anime'

export function useAnimeList() {
  const animeList = ref<AdminAnime[]>([])
  const loading = ref(true)
  const error = ref<string | null>(null)
  const dialogVisible = ref(false)
  const editingAnime = ref<AdminAnime | undefined>(undefined)

  // 加载动画列表
  const loadAnimeList = async (
    reviewStatus?: ReviewStatus,
    year?: number,
    season?: Season
  ) => {
    try {
      loading.value = true
      const animes = await getAnimeList(reviewStatus, year, season)
      animeList.value = animes
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载动画列表失败'
    } finally {
      loading.value = false
    }
  }

  // 删除动画
  const handleDeleteAnime = async (animeId: number, onMappingsMove?: (mappings: any[]) => void) => {
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

      // 通知父组件处理映射移动
      if (onMappingsMove && mappingsToMove.length > 0) {
        onMappingsMove(mappingsToMove)
      }

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

  // 提交表单
  const handleSubmitForm = async (formData: any, _selectedReviewStatus?: ReviewStatus) => {
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
  const handleUpdateReviewStatus = async (
    animeId: number,
    reviewStatus: ReviewStatus,
    selectedReviewStatus?: ReviewStatus
  ) => {
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
        if (selectedReviewStatus && reviewStatus !== selectedReviewStatus) {
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

  // 添加映射到指定动画
  const addMappingToAnime = (animeId: number, mapping: any) => {
    const anime = animeList.value.find(a => a.animeId === animeId)
    if (anime && !anime.mappings.some(m => m.mappingId === mapping.mappingId)) {
      anime.mappings.push(mapping)
    }
  }

  // 从动画中移除映射
  const removeMappingFromAnime = (animeId: number, mappingId: number) => {
    const anime = animeList.value.find(a => a.animeId === animeId)
    if (anime) {
      anime.mappings = anime.mappings.filter(m => m.mappingId !== mappingId)
    }
  }

  return {
    animeList,
    loading,
    error,
    dialogVisible,
    editingAnime,
    loadAnimeList,
    handleDeleteAnime,
    handleCreateAnime,
    handleEditAnime,
    handleCloseDialog,
    handleSubmitForm,
    handleUpdateReviewStatus,
    addMappingToAnime,
    removeMappingFromAnime
  }
}
