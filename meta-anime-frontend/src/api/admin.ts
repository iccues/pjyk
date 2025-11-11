/**
 * 管理后台 API 统一导出
 *
 * 按照后端 Controller 的结构拆分：
 * - adminAnime.ts: AdminAnimeController 相关接口
 * - adminMapping.ts: AdminMappingController 相关接口
 */

// 导出 Anime 相关 API
export {
    getAnimeList,
    createAnime,
    updateAnime,
    deleteAnime,
    type AnimeCreateRequest,
    type AnimeUpdateRequest
} from './adminAnime';

// 导出 Mapping 相关 API
export {
    getUnmappedMappingList,
    updateMappingAnime
} from './adminMapping';
