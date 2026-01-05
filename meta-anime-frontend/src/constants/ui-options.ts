/**
 * UI 选项常量
 *
 * 此文件包含用于 UI 组件的选项常量，如下拉菜单、筛选器等。
 */

import type { ReviewStatus } from "@/types/adminAnime";
import type { Season, SortBy } from "@/types/anime";

/**
 * 季度选项（含"全部"选项，用于筛选器）
 */
export const SEASON_OPTIONS: { label: string; value: Season | undefined }[] = [
  { label: "全部", value: undefined },
  { label: "冬季", value: "WINTER" },
  { label: "春季", value: "SPRING" },
  { label: "夏季", value: "SUMMER" },
  { label: "秋季", value: "FALL" },
];

/**
 * 季度选项（不含"全部"选项，用于数据抓取等必选场景）
 */
export const SEASON_OPTIONS_REQUIRED: { label: string; value: Season }[] = [
  { label: "冬季", value: "WINTER" },
  { label: "春季", value: "SPRING" },
  { label: "夏季", value: "SUMMER" },
  { label: "秋季", value: "FALL" },
];

/**
 * 审核状态选项
 */
export const REVIEW_STATUS_OPTIONS: {
  label: string;
  value: ReviewStatus | undefined;
}[] = [
  { label: "全部", value: undefined },
  { label: "待审核", value: "PENDING" },
  { label: "已通过", value: "APPROVED" },
  { label: "已拒绝", value: "REJECTED" },
];

/**
 * 排序选项
 */
export const SORT_BY_OPTIONS: { label: string; value: SortBy }[] = [
  { label: "按评分", value: "SCORE" },
  { label: "按人气", value: "POPULARITY" },
];
