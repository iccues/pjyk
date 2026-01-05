export interface Anime {
  animeId: number;
  title: AnimeTitles;
  coverImage: string;
  averageScore: number;
  mappings: Mapping[];
}

export interface Mapping {
  mappingId: number;
  sourcePlatform: string;
  platformId: string;
  rawScore: number;
}

export interface AnimeTitles {
  titleNative: string;
  titleRomaji: string;
  titleEn: string;
  titleCn: string;
}

export type Season = "WINTER" | "SPRING" | "SUMMER" | "FALL";

export type SortBy = "SCORE" | "POPULARITY";
