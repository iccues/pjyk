import type { AdminAnime, ReviewStatus } from "@/types/adminAnime";
import type { Season } from "@/types/anime";
import { del, get, post, put } from "./adminHttp";

/**
 * 获取所有动画列表（管理后台）
 * @param reviewStatus 可选的审核状态筛选
 * @param year 可选的年份筛选
 * @param season 可选的季度筛选
 */
export async function getAnimeList(
  reviewStatus?: ReviewStatus,
  year?: number,
  season?: Season,
): Promise<AdminAnime[]> {
  const params: Record<string, string> = {};
  if (reviewStatus !== undefined) params.reviewStatus = reviewStatus;
  if (year !== undefined) params.year = year.toString();
  if (season !== undefined) params.season = season;

  const options = Object.keys(params).length > 0 ? { params } : undefined;
  return get<AdminAnime[]>("/api/admin/get_anime_list", options);
}

/**
 * 创建动画
 */
export interface AnimeCreateRequest {
  title: {
    titleCn?: string;
    titleNative?: string;
    titleRomaji?: string;
    titleEn?: string;
  };
  coverImage?: string;
  startDate?: string;
}

export async function createAnime(
  request: AnimeCreateRequest,
): Promise<AdminAnime> {
  return post<AdminAnime>("/api/admin/create_anime", request);
}

/**
 * 更新动画
 */
export interface AnimeUpdateRequest {
  animeId: number;
  title?: {
    titleCn?: string;
    titleNative?: string;
    titleRomaji?: string;
    titleEn?: string;
  };
  coverImage?: string;
  startDate?: string;
  reviewStatus?: ReviewStatus;
}

export async function updateAnime(
  request: AnimeUpdateRequest,
): Promise<AdminAnime> {
  return put<AdminAnime>("/api/admin/update_anime", request);
}

/**
 * 删除动画
 * @param animeId 动画 ID
 */
export async function deleteAnime(animeId: number): Promise<void> {
  return del<void>(`/api/admin/delete_anime/${animeId}`);
}
