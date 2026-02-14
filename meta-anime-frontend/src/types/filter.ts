import type { Season, SortBy } from "./anime";

export interface AnimeFilterParams {
  year?: number;
  season?: Season;
  sortBy?: SortBy;
}

export const DEFAULT_FILTERS: AnimeFilterParams = {
  year: undefined,
  season: undefined,
  sortBy: "SCORE",
};
