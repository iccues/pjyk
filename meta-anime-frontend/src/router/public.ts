export default [
  {
    path: "/",
    component: () => import("@/layouts/PublicLayout.vue"),
    children: [
      {
        path: "",
        component: () => import("@/pages/public/HomePage.vue"),
      },
      {
        path: "anime/list",
        component: () => import("@/pages/public/AnimeListPage.vue"),
      },
      {
        path: "docs/about",
        component: () => import("@/pages/public/docs/AboutPage.vue"),
      },
      {
        path: "docs/metric",
        component: () => import("@/pages/public/docs/MetricPage.vue"),
      },
      {
        path: "docs/platforms",
        component: () => import("@/pages/public/docs/PlatformsPage.vue"),
      },
      {
        path: ":pathMatch(.*)*",
        component: () => import("@/pages/public/NotFoundPage.vue"),
      },
    ],
  },
];
