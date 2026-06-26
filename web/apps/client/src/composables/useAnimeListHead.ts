import { SEASON_NAME_MAP, SORT_BY_NAME_MAP } from "@pjyk-web/shared/constants/ui-options.ts";
import { useHead } from "@unhead/vue";
import type { Ref } from "vue";
import { computed } from "vue";

import type { GetAnimeListQueryVariables } from "@/graphql/generated/graphql";
import { buildSeoHead } from "@/utils/seoUtils";

export function useAnimeListHead(animeListParams: Ref<GetAnimeListQueryVariables>) {
  const pageTitle = computed(() => {
    const parts: string[] = [];

    if (animeListParams.value.year) {
      parts.push(`${animeListParams.value.year}年`);
      if (animeListParams.value.season) {
        parts.push(SEASON_NAME_MAP[animeListParams.value.season]);
      }
      parts.push("动漫");
    } else {
      parts.push("全部动漫");
    }

    if (animeListParams.value.sortBy === "POPULARITY") {
      parts.push(" - 按人气排序");
    }

    return `${parts.join("")} - 有希计划`;
  });

  const pageDescription = computed(() => {
    const parts: string[] = ["浏览"];

    if (animeListParams.value.year && animeListParams.value.season) {
      parts.push(`${animeListParams.value.year}年${SEASON_NAME_MAP[animeListParams.value.season]}`);
    } else if (animeListParams.value.year) {
      parts.push(`${animeListParams.value.year}年`);
    } else if (animeListParams.value.season) {
      parts.push(SEASON_NAME_MAP[animeListParams.value.season]);
    }

    parts.push(`动漫列表，按${SORT_BY_NAME_MAP[animeListParams.value.sortBy || "SCORE"]}排序。`);
    parts.push(
      "有希计划整合 Bangumi、MyAnimeList、AniList 多平台数据，为您提供更客观、更全面的番剧评价参考。",
    );

    return parts.join("");
  });

  const pageUrl = computed(() => {
    const url = new URL("https://www.yukiani.com/anime/list");
    if (animeListParams.value.year)
      url.searchParams.set("year", animeListParams.value.year.toString());
    if (animeListParams.value.season) url.searchParams.set("season", animeListParams.value.season);
    if (animeListParams.value.sortBy !== "SCORE")
      url.searchParams.set("sortBy", animeListParams.value.sortBy || "SCORE");
    return url.toString();
  });

  const pageKeywords = computed(() => {
    const keywords = ["动漫列表", "anime", "番剧", "动画评分"];
    if (animeListParams.value.year) keywords.push(`${animeListParams.value.year}年动漫`);
    if (animeListParams.value.season)
      keywords.push(`${SEASON_NAME_MAP[animeListParams.value.season]}新番`);
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
