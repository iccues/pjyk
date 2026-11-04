import { UserManager, WebStorageStateStore } from "oidc-client-ts";

import { getOidcConfig } from "@/api/config";

export const oidcConfig = await getOidcConfig();

export const oidcManager = oidcConfig.enabled
  ? new UserManager({
      authority: oidcConfig.issuer,
      client_id: oidcConfig.clientId,
      redirect_uri: `${location.origin}/auth/callback`,
      response_type: "code",
      scope: "openid profile email",
      automaticSilentRenew: false,
      userStore: new WebStorageStateStore({ store: window.localStorage }),
    })
  : null;

export async function login() {
  await oidcManager?.signinRedirect();
}

export function logout() {
  return oidcManager?.signoutRedirect();
}
