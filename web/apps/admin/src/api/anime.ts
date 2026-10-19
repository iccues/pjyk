import type { AdminAnime, ReviewStatus } from "@/types/adminAnime";
import type { Season } from "@/types/anime";

import adminClient from "./request";

/**
 * 获取所有动画列表（管理后台）
 * @param reviewStatus 可选的审核状态筛选，空字符串表示全部
 * @param year 可选的年份筛选
 * @param season 可选的季度筛选，空字符串表示全部
 */
export async function getAnimeList(
  reviewStatus?: ReviewStatus | "",
  year?: number | null,
  season?: Season | "",
  signal?: AbortSignal,
): Promise<AdminAnime[]> {
  const params: Record<string, string> = {};
  if (reviewStatus) params.reviewStatus = reviewStatus;
  if (year) params.year = year.toString();
  if (season) params.season = season;

  const response = await adminClient.get<AdminAnime[]>("/api/admin/animes", {
    params,
    signal,
  });
  return response.data;
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

export async function createAnime(request: AnimeCreateRequest): Promise<AdminAnime> {
  const response = await adminClient.post<AdminAnime>("/api/admin/animes", request);
  return response.data;
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

export async function updateAnime(request: AnimeUpdateRequest): Promise<AdminAnime> {
  const response = await adminClient.put<AdminAnime>("/api/admin/animes", request);
  return response.data;
}

/**
 * 删除动画
 * @param animeId 动画 ID
 */
export async function deleteAnime(animeId: number): Promise<void> {
  const response = await adminClient.delete<void>(`/api/admin/animes/${animeId}`);
  return response.data;
}
