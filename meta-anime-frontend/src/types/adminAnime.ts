export interface AdminAnime {
    animeId: number,
    title: AnimeTitles,
    coverImage: string,
    averageScore: number,
    mappings: AdminMapping[]
}

export interface AdminMapping {
    mappingId: number,
    animeId: number | null,
    sourcePlatform: string,
    platformId: string,
    rawScore: number,
    rawJSON: string,
}

export interface AnimeTitles {
    titleNative: string,
    titleRomaji: string,
    titleEn: string,
    titleCn: string,
}
