import { useHead } from "@unhead/vue";
import type { Ref } from "vue";
import { computed } from "vue";

import type { GetAnimeListBySearchQueryVariables } from "@/graphql/generated/graphql";
import { buildSeoHead } from "@/utils/seoUtils";

export function useSearchHead(searchParams: Ref<GetAnimeListBySearchQueryVariables>) {
  const pageTitle = computed(() => {
    const query = searchParams.value.query?.trim();
    if (query) {
      return `${query} - 搜索结果 - 有希计划`;
    }
    return "搜索动漫 - 有希计划";
  });

  const pageDescription = computed(() => {
    const query = searchParams.value.query?.trim();
    if (query) {
      return `在有希计划上搜索关于“${query}”的番剧。Meta Anime 聚合多个数据源，为您提供最全面的动漫信息和评分。`;
    }
    return "在有希计划上搜索番剧。Meta Anime 聚合多个数据源，为您提供最全面的动漫信息和评分。";
  });

  const pageUrl = computed(() => {
    const url = new URL("https://www.yukiani.com/search");
    const query = searchParams.value.query?.trim();
    if (query) {
      url.searchParams.set("q", query);
    }
    if (searchParams.value.pageNumber) {
      url.searchParams.set("page", searchParams.value.pageNumber.toString());
    }
    return url.toString();
  });

  const pageKeywords = computed(() => {
    const keywords = ["动漫搜索", "anime search", "番剧查询", "有希计划"];
    const query = searchParams.value.query?.trim();
    if (query) {
      keywords.push(query);
    }
    return keywords.join(",");
  });

  useHead(
    buildSeoHead({
      title: pageTitle,
      description: pageDescription,
      url: pageUrl,
      keywords: pageKeywords,
    }),
  );
}
