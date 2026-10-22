import { computed } from "vue";
import { useRoute, type RouteLocationRaw } from "vue-router";

import type { GetAnimeListQueryVariables } from "@/graphql/generated/graphql";
import { filtersToQuery, queryToFilters } from "@/utils/queryUtils";

export function useAnimeListQuery() {
  const route = useRoute();

  // 列表查询参数，由当前路由的 query 计算
  const animeListParams = computed<GetAnimeListQueryVariables>(() => ({
    ...queryToFilters(route.query),
    pageSize: 30,
  }));

  // 根据筛选变化生成链接，并重置到第 1 页
  const createFilterLink = (changes: Partial<GetAnimeListQueryVariables>): RouteLocationRaw => {
    const filters: GetAnimeListQueryVariables = {
      ...animeListParams.value,
      ...changes,
      pageNumber: 0,
    };

    if ("year" in changes && changes.year == null) {
      filters.season = undefined;
    }

    return {
      query: filtersToQuery(filters),
    };
  };

  // 根据 0-base 页码生成分页链接
  const createPageLink = (pageNumber: number): RouteLocationRaw => {
    return {
      query: filtersToQuery({
        ...animeListParams.value,
        pageNumber,
      }),
    };
  };

  return {
    animeListParams,
    createFilterLink,
    createPageLink,
  };
}
