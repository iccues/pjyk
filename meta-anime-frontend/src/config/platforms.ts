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
    'AniList': {
        name: 'AniList',
        logo: '/src/assets/logo/anilist.png',
        baseUrl: 'https://anilist.co',
        getAnimeUrl: (platformId: string) => `https://anilist.co/anime/${platformId}`
    },
    'Bangumi': {
        name: 'Bangumi',
        logo: '/src/assets/logo/bangumi.png',
        baseUrl: 'https://bgm.tv',
        getAnimeUrl: (platformId: string) => `https://bgm.tv/subject/${platformId}`
    },
    'MyAnimeList': {
        name: 'MyAnimeList',
        logo: '/src/assets/logo/myanimelist.png',
        baseUrl: 'https://myanimelist.net',
        getAnimeUrl: (platformId: string) => `https://myanimelist.net/anime/${platformId}`
    }
};

/**
 * 根据平台名称获取平台配置
 * @param platformName 平台名称
 * @returns 平台配置对象
 */
export function getPlatformConfig(platformName: string): PlatformConfig {
    return platformConfigs[platformName] || { name: platformName };
}
