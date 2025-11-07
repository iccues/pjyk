export interface Anime {
    animeId: number,
    title: AnimeTitles,
    coverImage: string,
    averageScore: number,
    mappings: [AnimeMapping]
}

export interface AnimeMapping {
    mappingId: number,
    sourcePlatform: string,
    platformId: string,
    rawScore: number,
}

export interface AnimeTitles {
    titleNative: string,
    titleRomaji: string,
    titleEn: string,
    titleCn: string,
}
