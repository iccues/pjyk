import { createRouter, createWebHistory } from "vue-router";

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
        component: () => import("@/docs/pages/AboutPage.vue"),
      },
      {
        path: "docs/metric",
        component: () => import("@/docs/pages/MetricPage.vue"),
      },
      {
        path: "docs/platforms",
        component: () => import("@/docs/pages/PlatformsPage.vue"),
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
