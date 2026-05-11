import type { UseHeadInput } from "@unhead/vue";
import type { ComputedRef } from "vue";

type MaybeComputed<T> = ComputedRef<T> | T;

export interface SeoOptions {
  title: MaybeComputed<string>;
  description: MaybeComputed<string>;
  url?: MaybeComputed<string>;
  keywords?: MaybeComputed<string>;
}

/**
 * 生成通用的 SEO head 配置对象，包含 meta description/keywords、
 * Open Graph、Twitter Card 以及 canonical link（url 可选）。
 *
 * 可直接传入 useHead()：
 *   useHead(buildSeoHead({ title, description, url, keywords }))
 */
export function buildSeoHead({ title, description, url, keywords }: SeoOptions): UseHeadInput {
  const meta = [
    { key: "description", name: "description", content: description },
    // Open Graph
    { key: "og:title", property: "og:title", content: title },
    { key: "og:description", property: "og:description", content: description },
    // Twitter Card
    { key: "twitter:title", name: "twitter:title", content: title },
    { key: "twitter:description", name: "twitter:description", content: description },
  ];

  if (keywords !== undefined) {
    meta.splice(1, 0, {
      key: "keywords",
      name: "keywords",
      content: keywords,
    });
  }

  if (url !== undefined) {
    meta.splice(
      meta.findIndex((m: any) => m.name === "twitter:title"),
      0,
      {
        key: "og:url",
        property: "og:url",
        content: url,
      },
    );
  }

  return {
    title,
    meta,
    ...(url !== undefined ? { link: [{ rel: "canonical", href: url }] } : {}),
  };
}
