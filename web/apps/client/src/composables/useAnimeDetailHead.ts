import { useHead } from "@unhead/vue";
import { computed, type Ref } from "vue";

import type { GetAnimeDetailQuery } from "@/graphql/generated/graphql";
import { buildSeoHead } from "@/utils/seoUtils";

export function useAnimeDetailHead(anime: Ref<GetAnimeDetailQuery["anime"] | undefined>) {
  const pageTitle = computed(() => {
    if (!anime.value) return "有希计划";
    const t = anime.value.title;
    const mainTitle = t.titleCn || t.titleNative || t.titleRomaji || t.titleEn || "未命名番剧";
    return `${mainTitle} - 有希计划`;
  });

  const pageDescription = computed(() => {
    if (!anime.value) return "加载番剧详情中...";
    const t = anime.value.title;
    const title = t.titleCn || t.titleNative || "";
    let desc = `在有希计划中查看《${title}》的详细信息。`;
    if (anime.value.averageScore) desc += ` 综合评分：${anime.value.averageScore.toFixed(1)}/100。`;
    if (anime.value.startDate) desc += ` 开播日期：${anime.value.startDate}。`;
    desc += " Meta Anime 聚合多个数据源，为您提供最全面的动漫信息和评分。";
    return desc;
  });

  const pageKeywords = computed(() => {
    if (!anime.value) return "动漫,anime,番剧";
    const t = anime.value.title;
    const keywords = ["动漫", "anime", "番剧"];
    if (t.titleCn) keywords.push(t.titleCn);
    if (t.titleNative) keywords.push(t.titleNative);
    if (t.titleRomaji) keywords.push(t.titleRomaji);
    return keywords.join(",");
  });

  const pageUrl = computed(() => {
    if (!anime.value) return "https://www.yukiani.com";
    return `https://www.yukiani.com/anime/${anime.value.animeId}`;
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
