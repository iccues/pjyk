import type { NavigationGuardNext, RouteLocationNormalized } from "vue-router";

export async function authGuard(
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext,
) {
  if (!to.meta.requiresAuth) {
    return next();
  }

  const { oidcManager } = await import("./oidc");
  const user = await oidcManager.getUser();
  if (!user || user.expired) {
    return next({
      path: "/admin/auth/login",
    });
  }

  return next();
}
