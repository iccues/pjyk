import { get, put } from './http';
import type { AdminMapping } from '../types/adminAnime';

/**
 * 获取未关联的孤立映射列表（管理后台）
 */
export async function getUnmappedMappingList(): Promise<AdminMapping[]> {
    return get<AdminMapping[]>('/api/admin/get_unmapped_mapping_list');
}

/**
 * 更新映射的动画关联
 * @param mappingId 映射 ID
 * @param animeId 动画 ID（null 表示解除关联）
 */
export async function updateMappingAnime(
    mappingId: number,
    animeId: number | null
): Promise<AdminMapping> {
    return put<AdminMapping>('/api/admin/update_mapping_anime', {
        mappingId,
        animeId
    });
}
