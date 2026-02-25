import type { LocationQuery } from "vue-router";
import type { AnimeListParams } from "@/api/public/anime";
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
 * Parse page from query string (0-based index, defaults to 0)
 */
const parsePage = (value: string): number => {
  const num = parseInt(value);
  return isNaN(num) || num < 0 ? 0 : num;
};

/**
 * Convert URL query params to AnimeListParams (filters + page)
 */
export const queryToFilters = (query: LocationQuery): AnimeListParams => {
  const filters: AnimeListParams = {
    year: undefined,
    season: undefined,
    sortBy: "SCORE",
    page: 0,
  };

  if (query.year && typeof query.year === "string") {
    filters.year = parseYear(query.year);
  }

  if (query.season && typeof query.season === "string") {
    filters.season = parseSeason(query.season);
  }

  if (query.sortBy && typeof query.sortBy === "string") {
    const parsed = parseSortBy(query.sortBy);
    if (parsed !== undefined) {
      filters.sortBy = parsed;
    }
  }

  if (query.page && typeof query.page === "string") {
    filters.page = parsePage(query.page);
  }

  return filters;
};

/**
 * Convert AnimeListParams (filters + page) to URL query params
 */
export const filtersToQuery = (filters: AnimeListParams): Record<string, string> => {
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

  if (filters.page !== undefined && filters.page > 0) {
    query.page = filters.page.toString();
  }

  return query;
};
