import { ref, computed } from 'vue'
import type { ReviewStatus } from '@/types/adminAnime'
import type { Season } from '@/types/anime'

export function useAnimeFilters() {
  // 筛选器状态
  const selectedReviewStatus = ref<ReviewStatus | undefined>('PENDING')
  const selectedYear = ref<number | undefined>(undefined)
  const selectedSeason = ref<Season | undefined>(undefined)

  // 审核状态选项
  const reviewStatusOptions = [
    { label: '全部', value: undefined },
    { label: '待审核', value: 'PENDING' as ReviewStatus },
    { label: '已通过', value: 'APPROVED' as ReviewStatus },
    { label: '已拒绝', value: 'REJECTED' as ReviewStatus }
  ]

  // 季度选项
  const seasonOptions = [
    { label: '全部', value: undefined },
    { label: '冬季', value: 'WINTER' as Season },
    { label: '春季', value: 'SPRING' as Season },
    { label: '夏季', value: 'SUMMER' as Season },
    { label: '秋季', value: 'FALL' as Season }
  ]

  // 生成年份选项（从当前年份往前推10年）
  const currentYear = new Date().getFullYear()
  const yearOptions = computed(() => {
    const years: { label: string; value: number | undefined }[] = [{ label: '全部', value: undefined }]
    for (let i = 0; i <= 10; i++) {
      const year = currentYear - i
      years.push({ label: `${year}年`, value: year })
    }
    return years
  })

  return {
    selectedReviewStatus,
    selectedYear,
    selectedSeason,
    reviewStatusOptions,
    seasonOptions,
    yearOptions
  }
}
