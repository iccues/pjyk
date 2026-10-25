import { createRouter, createWebHistory } from "vue-router";

// 通用文档页，每篇文档在路由 props 中传入 md 加载函数和 SEO 参数
const DocPage = () => import("@/docs/DocPage.vue");

const routes = [
  {
    path: "/",
    component: () => import("@/layouts/PublicLayout.vue"),
    children: [
      {
        path: "",
        component: () => import("@/pages/HomePage.vue"),
      },
      {
        path: "anime/list",
        component: () => import("@/pages/AnimeListPage.vue"),
      },
      {
        path: "search",
        component: () => import("@/pages/SearchPage.vue"),
      },
      {
        path: "anime/:animeId",
        component: () => import("@/pages/AnimeDetailPage.vue"),
      },
      {
        path: "docs/about",
        component: DocPage,
        props: {
          load: () => import("@/docs/content/about.md?raw"),
          title: "关于 - 有希计划",
          description:
            "有希计划是一个整合 Bangumi、MyAnimeList、AniList 多平台数据的动画综合评分平台。",
        },
      },
      {
        path: "docs/metric",
        component: DocPage,
        props: {
          load: () => import("@/docs/content/metric.md?raw"),
          title: "评分与热度说明 - 有希计划",
          description:
            "了解有希计划的动画评分与热度数据是如何通过多平台归一化处理后综合计算得出的。",
        },
      },
      {
        path: "docs/platforms",
        component: DocPage,
        props: {
          load: () => import("@/docs/content/platforms.md?raw"),
          title: "平台数据参数 - 有希计划",
          description: "查看有希计划各评分平台的权重、评分均值、标准差及热度倍率等数据参数。",
        },
      },
      {
        path: ":pathMatch(.*)*",
        component: () => import("@/pages/NotFoundPage.vue"),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

export default router;
