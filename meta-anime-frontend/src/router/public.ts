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
    ],
  },
];
