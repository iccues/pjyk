import { computed, ref, watch } from "vue";
import { useRoute, useRouter, type RouteLocationRaw } from "vue-router";

import type { GetAnimeListBySearchQueryVariables } from "@/graphql/generated/graphql";

export function useSearchQuery() {
  const router = useRouter();
  const route = useRoute();

  // 搜索输入
  const searchInput = ref((route.query.q as string) || "");

  // 搜索查询参数，由当前路由的 query 计算
  const searchParams = computed<GetAnimeListBySearchQueryVariables>(() => ({
    keyword: (route.query.q as string) || "",
    pageNumber: route.query.page ? parseInt(route.query.page as string, 10) : 0,
    pageSize: 30,
  }));

  // 路由变化时同步搜索输入
  watch(
    () => route.query,
    (newQuery) => {
      searchInput.value = (newQuery.q as string) || "";
    },
    { deep: true },
  );

  // 处理搜索提交
  const handleSearch = () => {
    const keyword = searchInput.value.trim();
    // 空关键词会清除 URL 中的查询参数。
    router.push({ query: keyword ? { q: keyword, page: 0 } : {} });
  };

  // 根据 0-base 页码生成分页链接
  const createPageLink = (pageNumber: number): RouteLocationRaw => {
    return {
      query: {
        ...route.query,
        page: pageNumber,
      },
    };
  };

  return {
    searchInput,
    searchParams,
    handleSearch,
    createPageLink,
  };
}
