import type { RouteLocationNormalized } from "vue-router";

import { oidcConfig, oidcManager } from "./oidc";

export async function authGuard(to: RouteLocationNormalized) {
  if (!oidcConfig.enabled) {
    if (to.path === "/auth/login" || to.path === "/auth/callback") {
      return "/";
    }
    return true;
  }

  if (!to.meta.requiresAuth) {
    return true;
  }

  const user = await oidcManager?.getUser();
  if (!user || user.expired) {
    return {
      path: "/auth/login",
    };
  }

  return true;
}
