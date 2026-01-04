/**
 * 枚举映射常量
 *
 * 此文件包含将后端枚举值映射为前端显示文本的常量。
 */

import type { ReviewStatus } from "@/types/adminAnime";
import type { Season } from "@/types/anime";

/**
 * 季度枚举值到中文名称的映射
 */
export const SEASON_NAME_MAP: Record<Season, string> = {
  WINTER: "冬季",
  SPRING: "春季",
  SUMMER: "夏季",
  FALL: "秋季",
};

/**
 * 审核状态枚举值到中文名称的映射
 */
export const REVIEW_STATUS_NAME_MAP: Record<ReviewStatus, string> = {
  PENDING: "待审核",
  APPROVED: "已通过",
  REJECTED: "已拒绝",
};
