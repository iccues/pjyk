import { get, post, put, del } from './http';
import type { AdminAnime } from '../types/adminAnime';

/**
 * 获取所有动画列表（管理后台）
 */
export async function getAnimeList(): Promise<AdminAnime[]> {
    return get<AdminAnime[]>('/api/admin/get_anime_list');
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
    return post<AdminAnime>('/api/admin/create_anime', request);
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
}

export async function updateAnime(request: AnimeUpdateRequest): Promise<AdminAnime> {
    return put<AdminAnime>('/api/admin/update_anime', request);
}

/**
 * 删除动画
 * @param animeId 动画 ID
 */
export async function deleteAnime(animeId: number): Promise<void> {
    return del<void>(`/api/admin/delete_anime/${animeId}`);
}
