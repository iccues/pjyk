import { UserManager, WebStorageStateStore } from "oidc-client-ts";

import { getOidcConfig } from "@/api/public/config";

const oidcConfig = await getOidcConfig();

export const oidcManager = new UserManager({
  authority: oidcConfig.issuer,
  client_id: oidcConfig.clientId,
  redirect_uri: `${location.origin}/auth/callback`,
  response_type: "code",
  scope: "openid profile email",
  automaticSilentRenew: false,
  userStore: new WebStorageStateStore({ store: window.localStorage }),
});

export async function login() {
  await oidcManager.signinRedirect();
}

export function logout() {
  oidcManager.signoutRedirect();
}
