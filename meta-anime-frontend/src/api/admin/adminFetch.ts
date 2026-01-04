import { post } from "./adminHttp";

/**
 * 抓取动画数据（完整流程：抓取映射、合并映射、计算评分）
 * @param year 年份
 * @param season 季度（SPRING, SUMMER, FALL, WINTER）
 */
export async function fetchAnime(
  year: number,
  season: string,
): Promise<string> {
  return post<string>("/api/admin/fetch/anime", null, {
    params: { year: year.toString(), season },
  });
}

/**
 * 抓取映射数据
 * @param year 年份
 * @param season 季度（SPRING, SUMMER, FALL, WINTER）
 */
export async function fetchMapping(
  year: number,
  season: string,
): Promise<string> {
  return post<string>("/api/admin/fetch/mapping", null, {
    params: { year: year.toString(), season },
  });
}

/**
 * 合并映射数据
 */
export async function linkMappings(): Promise<string> {
  return post<string>("/api/admin/fetch/link");
}

/**
 * 计算所有动画的平均评分
 */
export async function calculateScores(): Promise<string> {
  return post<string>("/api/admin/fetch/calculate_scores");
}
