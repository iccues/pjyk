import { useHead } from "@unhead/vue";
import type { Ref } from "vue";
import { computed } from "vue";

import type { GetAnimeListBySearchQueryVariables } from "@/graphql/generated/graphql";
import { buildSeoHead } from "@/utils/seoUtils";

export function useSearchHead(searchParams: Ref<GetAnimeListBySearchQueryVariables>) {
  const pageTitle = computed(() => {
    const keyword = searchParams.value.keyword?.trim();
    if (keyword) {
      return `${keyword} - 搜索结果 - 有希计划`;
    }
    return "搜索动漫 - 有希计划";
  });

  const pageDescription = computed(() => {
    const keyword = searchParams.value.keyword?.trim();
    if (keyword) {
      return `在有希计划上搜索关于“${keyword}”的番剧。有希计划整合 Bangumi、MyAnimeList、AniList 多平台数据，为您提供更客观、更全面的番剧评价参考。`;
    }
    return "在有希计划上搜索番剧。有希计划整合 Bangumi、MyAnimeList、AniList 多平台数据，为您提供更客观、更全面的番剧评价参考。";
  });

  const pageUrl = computed(() => {
    const url = new URL("https://www.yukiani.com/search");
    const keyword = searchParams.value.keyword?.trim();
    if (keyword) {
      url.searchParams.set("q", keyword);
    }
    if (searchParams.value.pageNumber) {
      url.searchParams.set("page", searchParams.value.pageNumber.toString());
    }
    return url.toString();
  });

  const pageKeywords = computed(() => {
    const keywords = ["动漫搜索", "anime search", "番剧查询", "有希计划"];
    const keyword = searchParams.value.keyword?.trim();
    if (keyword) {
      keywords.push(keyword);
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
