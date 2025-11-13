import type { AnimeTitles } from "./anime";

export interface AdminAnime {
    animeId: number,
    title: AnimeTitles,
    coverImage: string,
    startDate: string,
    averageScore: number,
    reviewStatus: ReviewStatus,
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

export type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
