import { get } from './http';
import type { Anime, Season } from '@/types/anime';
import type { Page } from '@/types/page';

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
export async function getAnimeList(params?: AnimeListParams): Promise<Page<Anime>> {
    const queryParams: any = {};

    if (params?.year !== undefined) queryParams.year = params.year;
    if (params?.season !== undefined) queryParams.season = params.season;
    if (params?.page !== undefined) queryParams.page = params.page;
    if (params?.pageSize !== undefined) queryParams.pageSize = params.pageSize;

    const options = Object.keys(queryParams).length > 0 ? { params: queryParams } : undefined;
    return get<Page<Anime>>('/api/anime/get_list', options);
}