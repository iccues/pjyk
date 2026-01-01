import anilistLogo from "@/assets/logo/anilist.png";
import bangumiLogo from "@/assets/logo/bangumi.png";
import myAnimeListLogo from "@/assets/logo/myanimelist.png";

/**
 * 平台配置接口
 */
export interface PlatformConfig {
  name: string;
  logo?: string;
  baseUrl?: string;
  /**
   * 生成动画条目URL的函数
   * @param platformId 平台上的动画ID
   * @returns 完整的URL
   */
  getAnimeUrl?: (platformId: string) => string;
}

/**
 * 平台配置映射表
 */
const platformConfigs: Record<string, PlatformConfig> = {
  AniList: {
    name: "AniList",
    logo: anilistLogo,
    baseUrl: "https://anilist.co",
    getAnimeUrl: (platformId: string) =>
      `https://anilist.co/anime/${platformId}`,
  },
  Bangumi: {
    name: "Bangumi",
    logo: bangumiLogo,
    baseUrl: "https://bgm.tv",
    getAnimeUrl: (platformId: string) => `https://bgm.tv/subject/${platformId}`,
  },
  MyAnimeList: {
    name: "MyAnimeList",
    logo: myAnimeListLogo,
    baseUrl: "https://myanimelist.net",
    getAnimeUrl: (platformId: string) =>
      `https://myanimelist.net/anime/${platformId}`,
  },
};

/**
 * 根据平台名称获取平台配置
 * @param platformName 平台名称
 * @returns 平台配置对象
 */
export function getPlatformConfig(platformName: string): PlatformConfig {
  return platformConfigs[platformName] || { name: platformName };
}

/**
 * 获取所有支持的平台列表
 * @returns 平台名称数组
 */
export function getAllPlatforms(): string[] {
  return Object.keys(platformConfigs);
}

/**
 * 获取所有平台的配置列表（用于下拉选择等）
 * @returns 平台配置数组
 */
export function getAllPlatformConfigs(): PlatformConfig[] {
  return Object.values(platformConfigs);
}
