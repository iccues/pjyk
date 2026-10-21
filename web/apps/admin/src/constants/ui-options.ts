/**
 * UI 选项常量
 *
 * el-select 下拉框用到的选项。"全部"的 value 为空字符串，el-select 默认把空字符串视为未选择，
 * 选中"全部"时输入框显示 placeholder。
 */

import type { ReviewStatus, Season } from "@/types/adminAnime";

type SelectOption<T> = { label: string; value: T };

/**
 * 季度选项
 */
export const SEASON_OPTIONS: SelectOption<Season | "">[] = [
  { label: "全部", value: "" },
  { label: "1月", value: "WINTER" },
  { label: "4月", value: "SPRING" },
  { label: "7月", value: "SUMMER" },
  { label: "10月", value: "FALL" },
];

/**
 * 审核状态选项
 */
export const REVIEW_STATUS_OPTIONS: SelectOption<ReviewStatus | "">[] = [
  { label: "全部", value: "" },
  { label: "待审核", value: "PENDING" },
  { label: "已通过", value: "APPROVED" },
  { label: "已拒绝", value: "REJECTED" },
];

/**
 * 平台选项
 */
export const PLATFORM_OPTIONS: SelectOption<string>[] = [
  { label: "全部", value: "" },
  { label: "Bangumi", value: "Bangumi" },
  { label: "AniList", value: "AniList" },
  { label: "MyAnimeList", value: "MyAnimeList" },
];
