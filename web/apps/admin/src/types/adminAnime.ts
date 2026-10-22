export interface AnimeTitles {
  titleNative: string;
  titleRomaji: string;
  titleEn: string;
  titleCn: string;
}

export interface AdminAnime {
  animeId: number;
  title: AnimeTitles;
  coverImage: string;
  startDate: string;
  averageScore: number;
  reviewStatus: ReviewStatus;
  mappings: AdminMapping[];
}

export interface AdminMapping {
  mappingId: number;
  animeId: number | null;
  sourcePlatform: string;
  platformId: string;
  rawScore: number;
  mappingInfo: MappingInfo;
}

export interface MappingInfo {
  title: AnimeTitles;
  coverImage: string;
  startDate: string;
}

export type ReviewStatus = "PENDING" | "APPROVED" | "REJECTED";

export type Season = "WINTER" | "SPRING" | "SUMMER" | "FALL";
