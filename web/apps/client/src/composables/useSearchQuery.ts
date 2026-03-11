import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

import type { GetAnimeListBySearchQueryVariables } from "@/graphql/generated/graphql";

export function useSearchQuery() {
  const router = useRouter();
  const route = useRoute();

  // Hero Search 用的搜索文本
  const searchInput = ref((route.query.q as string) || "");

  // 查询参数
  const searchParams = ref<GetAnimeListBySearchQueryVariables>({
    query: searchInput.value,
    pageNumber: route.query.page ? parseInt(route.query.page as string, 10) : 0,
    pageSize: 30,
  });

  // 监听路由变化，同步到组件状态
  watch(
    () => route.query,
    (newQuery) => {
      searchInput.value = (newQuery.q as string) || "";
      searchParams.value = {
        query: searchInput.value,
        pageNumber: newQuery.page ? parseInt(newQuery.page as string, 10) : 0,
        pageSize: 30,
      };
    },
    { deep: true },
  );

  // 处理搜索提交
  const handleSearch = () => {
    const query = searchInput.value.trim();
    router.push({
      query: query ? { q: query, page: 0 } : {},
    });
  };

  // 处理分页
  const handlePageChange = (page: number) => {
    router.push({
      query: {
        ...route.query,
        page,
      },
    });
  };

  return {
    searchInput,
    searchParams,
    handleSearch,
    handlePageChange,
  };
}
