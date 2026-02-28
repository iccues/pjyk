import type { NavigationGuardNext, RouteLocationNormalized } from "vue-router";
import { oidcManager } from "./oidc";

export async function authGuard(
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext,
) {
  if (!to.meta.requiresAuth) {
    return next();
  }

  const user = await oidcManager.getUser();
  if (!user || user.expired) {
    return next({
      path: "/auth/login",
    });
  }

  return next();
}
