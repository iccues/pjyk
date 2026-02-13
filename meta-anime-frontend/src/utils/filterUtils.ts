import type { LocationQuery } from "vue-router";
import type { AnimeFilterParams } from "@/types/filter";
import type { Season, SortBy } from "@/types/anime";

/**
 * Parse year from query string
 */
const parseYear = (value: string): number | undefined => {
  const num = parseInt(value);
  return isNaN(num) ? undefined : num;
};

/**
 * Parse season from query string
 */
const parseSeason = (value: string): Season | undefined => {
  const validSeasons: Season[] = ["WINTER", "SPRING", "SUMMER", "FALL"];
  return validSeasons.includes(value as Season) ? (value as Season) : undefined;
};

/**
 * Parse sortBy from query string
 */
const parseSortBy = (value: string): SortBy | undefined => {
  const validSortBy: SortBy[] = ["SCORE", "POPULARITY"];
  return validSortBy.includes(value as SortBy) ? (value as SortBy) : undefined;
};

/**
 * Convert URL query params to AnimeFilterParams
 */
export const queryToFilters = (query: LocationQuery): AnimeFilterParams => {
  const filters: AnimeFilterParams = {
    year: undefined,
    season: undefined,
    sortBy: "SCORE",
  };

  // Parse year
  if (query.year && typeof query.year === "string") {
    filters.year = parseYear(query.year);
  }

  // Parse season
  if (query.season && typeof query.season === "string") {
    filters.season = parseSeason(query.season);
  }

  // Parse sortBy
  if (query.sortBy && typeof query.sortBy === "string") {
    const parsed = parseSortBy(query.sortBy);
    if (parsed !== undefined) {
      filters.sortBy = parsed;
    }
  }

  return filters;
};

/**
 * Convert AnimeFilterParams to URL query params
 */
export const filtersToQuery = (filters: AnimeFilterParams): Record<string, string> => {
  const query: Record<string, string> = {};

  if (filters.year !== undefined) {
    query.year = filters.year.toString();
  }

  if (filters.season !== undefined) {
    query.season = filters.season;
  }

  if (filters.sortBy !== undefined && filters.sortBy !== "SCORE") {
    query.sortBy = filters.sortBy;
  }

  return query;
};
