/**
 * UI 选项常量
 *
 * 此文件包含用于 UI 组件的选项常量，如下拉菜单、筛选器等。
 */

export type ReviewStatus = "PENDING" | "APPROVED" | "REJECTED";
export type Season = "WINTER" | "SPRING" | "SUMMER" | "FALL";
export type SortBy = "SCORE" | "POPULARITY";
export type Platform = "AniList" | "Bangumi" | "MyAnimeList";

/**
 * 季度选项（含"全部"选项，用于筛选器）
 */
export const SEASON_OPTIONS: { label: string; value: Season | undefined }[] = [
  { label: "全部", value: undefined },
  { label: "1月", value: "WINTER" },
  { label: "4月", value: "SPRING" },
  { label: "7月", value: "SUMMER" },
  { label: "10月", value: "FALL" },
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

/**
 * 平台选项
 */
export const PLATFORM_OPTIONS: { label: string; value: Platform | undefined }[] = [
  { label: "全部", value: undefined },
  { label: "Bangumi", value: "Bangumi" },
  { label: "AniList", value: "AniList" },
  { label: "MyAnimeList", value: "MyAnimeList" },
];

/**
 * 季度名称映射（用于 SEO、文案等场景）
 */
export const SEASON_NAME_MAP: Record<Season, string> = {
  WINTER: "1月",
  SPRING: "4月",
  SUMMER: "7月",
  FALL: "10月",
};

/**
 * 排序方式名称映射（用于 SEO、文案等场景）
 */
export const SORT_BY_NAME_MAP: Record<SortBy, string> = {
  SCORE: "评分",
  POPULARITY: "人气",
};
