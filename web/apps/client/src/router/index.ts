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
        path: "docs/about",
        component: () => import("@/pages/docs/AboutPage.vue"),
      },
      {
        path: "docs/metric",
        component: () => import("@/pages/docs/MetricPage.vue"),
      },
      {
        path: "docs/platforms",
        component: () => import("@/pages/docs/PlatformsPage.vue"),
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
