import type { LocationQuery } from "vue-router";

import type { GetAnimeListQueryVariables, Season, SortBy } from "@/graphql/generated/graphql";

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
 * Convert URL query params to GetAnimeListQueryVariables (filters + page)
 */
export const queryToFilters = (query: LocationQuery): GetAnimeListQueryVariables => {
  const filters: GetAnimeListQueryVariables = {
    year: undefined,
    season: undefined,
    sortBy: "SCORE",
    pageNumber: 0,
  };

  if (query.year != null && typeof query.year === "string") {
    filters.year = parseYear(query.year);
  }

  if (query.season != null && typeof query.season === "string") {
    filters.season = parseSeason(query.season);
  }

  if (query.sortBy != null && typeof query.sortBy === "string") {
    const parsed = parseSortBy(query.sortBy);
    if (parsed !== undefined) {
      filters.sortBy = parsed;
    }
  }

  if (query.pageNumber != null && typeof query.pageNumber === "string") {
    filters.pageNumber = parsePage(query.pageNumber);
  }

  return filters;
};

/**
 * Convert AnimeListParams (filters + page) to URL query params
 */
export const filtersToQuery = (filters: GetAnimeListQueryVariables): Record<string, string> => {
  const query: Record<string, string> = {};

  if (filters.year != null) {
    query.year = filters.year.toString();
  }

  if (filters.season != null) {
    query.season = filters.season;
  }

  if (filters.sortBy != null && filters.sortBy !== "SCORE") {
    query.sortBy = filters.sortBy;
  }

  if (filters.pageNumber != null && filters.pageNumber > 0) {
    query.pageNumber = filters.pageNumber.toString();
  }

  return query;
};
