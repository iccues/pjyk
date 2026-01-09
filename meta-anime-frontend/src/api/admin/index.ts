/**
 * 管理后台 API 统一导出
 *
 * 按照后端 Controller 的结构拆分：
 * - adminAnime.ts: AdminAnimeController 相关接口
 * - adminMapping.ts: AdminMappingController 相关接口
 * - adminFetch.ts: AdminFetchController 相关接口
 */

// 导出 Anime 相关 API
export {
  type AnimeCreateRequest,
  type AnimeUpdateRequest,
  createAnime,
  deleteAnime,
  getAnimeList,
  updateAnime,
} from "./adminAnime";
// 导出 Fetch 相关 API
export {
  calculateMetric,
  fetchAnime,
  fetchMapping,
  linkMappings,
} from "./adminFetch";
// 导出 Mapping 相关 API
export {
  createMapping,
  deleteMapping,
  getUnmappedMappingList,
  updateMappingAnime,
} from "./adminMapping";
