import { UserManager } from "oidc-client-ts";
import { getOidcConfig } from "@/api/config";

const oidcConfig = await getOidcConfig();

export const oidcManager = new UserManager({
    authority: oidcConfig.issuer,
    client_id: oidcConfig.clientId,
    redirect_uri: `${location.origin}/admin/auth/callback`,
    response_type: 'code',
    scope: 'openid profile email',
    automaticSilentRenew: false,
});

export async function login() {
    await oidcManager.signinRedirect();
}

export function logout() {
    oidcManager.signoutRedirect();
}
