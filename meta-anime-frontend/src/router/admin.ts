export default [
  {
    path: "/admin",
    component: () => import("@/layouts/AdminLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      {
        path: "",
        component: () => import("@/pages/admin/AdminHomePage.vue"),
      },
      {
        path: "list",
        component: () => import("@/pages/admin/AdminListPage.vue"),
      },
      {
        path: "auth",
        component: () => import("@/pages/auth/AuthHome.vue"),
      },
    ],
  },

  {
    path: "/admin/auth/callback",
    component: () => import("@/pages/auth/Callback.vue"),
  },
  {
    path: "/admin/auth/login",
    component: () => import("@/pages/auth/Login.vue"),
  },
];
