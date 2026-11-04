import type { PlatformKey } from "@pjyk-web/shared/types/platformKey.ts";

import type { AdminMapping } from "@/types/adminAnime";

import adminClient from "./request";

/**
 * 获取未关联的孤立映射列表（管理后台）
 */
export async function getUnlinkedMappings(signal?: AbortSignal): Promise<AdminMapping[]> {
  const response = await adminClient.get<AdminMapping[]>("/api/admin/mappings/unlinked", {
    signal,
  });
  return response.data;
}

/**
 * 更新映射的动画关联
 * @param mappingId 映射 ID
 * @param animeId 动画 ID（null 表示解除关联）
 */
export async function updateMappingAnime(
  mappingId: number,
  animeId: number | null,
): Promise<AdminMapping> {
  const response = await adminClient.put<AdminMapping>("/api/admin/mappings/anime", {
    mappingId,
    animeId,
  });
  return response.data;
}

/**
 * 创建新的映射
 * @param platformKey 平台条目标识
 */
export async function createMapping(platformKey: PlatformKey): Promise<AdminMapping> {
  const response = await adminClient.post<AdminMapping>("/api/admin/mappings", platformKey);
  return response.data;
}

/**
 * 删除映射
 * @param mappingId 映射 ID
 */
export async function deleteMapping(mappingId: number): Promise<void> {
  const response = await adminClient.delete<void>(`/api/admin/mappings/${mappingId}`);
  return response.data;
}
