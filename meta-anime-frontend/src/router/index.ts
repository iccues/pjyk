import { createRouter, createWebHistory } from "vue-router";
import { authGuard } from "@/auth/authGuard";
import adminRoutes from "./admin";
import publicRoutes from "./public";

const routes = [...publicRoutes, ...adminRoutes];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(authGuard);

export default router;
