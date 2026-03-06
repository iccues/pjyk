import { createRouter, createWebHistory } from "vue-router";
import { authGuard } from "@/auth/authGuard";

const routes = [
  {
    path: "",
    component: () => import("@/layouts/AdminLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      {
        path: "",
        component: () => import("@/pages/AdminHomePage.vue"),
      },
      {
        path: "list",
        component: () => import("@/pages/AdminListPage.vue"),
      },
      {
        path: "auth",
        component: () => import("@/pages/auth/AuthHome.vue"),
      },
    ],
  },

  {
    path: "/auth/callback",
    component: () => import("@/pages/auth/Callback.vue"),
  },
  {
    path: "/auth/login",
    component: () => import("@/pages/auth/Login.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

router.beforeEach(authGuard);

export default router;
