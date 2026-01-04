import type { Anime, Season } from "@/types/anime";
import type { Page } from "@/types/page";
import { get } from "./http";

/**
 * 获取动画列表的查询参数
 */
export interface AnimeListParams {
  year?: number;
  season?: Season;
  page?: number;
  pageSize?: number;
}

/**
 * 获取动画列表（分页）
 * @param params 查询参数
 * @returns 分页的动画列表
 */
export async function getAnimeList(
  params?: AnimeListParams,
): Promise<Page<Anime>> {
  const queryParams: Record<string, string> = {};

  if (params?.year !== undefined) queryParams.year = params.year.toString();
  if (params?.season !== undefined) queryParams.season = params.season;
  if (params?.page !== undefined) queryParams.page = params.page.toString();
  if (params?.pageSize !== undefined)
    queryParams.pageSize = params.pageSize.toString();

  const options =
    Object.keys(queryParams).length > 0 ? { params: queryParams } : undefined;
  return get<Page<Anime>>("/api/anime/get_list", options);
}
