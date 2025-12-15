import { UserManager } from "oidc-client-ts";
import { getOidcConfig } from "../api/config";

const oidcConfig = await getOidcConfig();

console.log("OIDC Config:", oidcConfig);

export const oidcManager = new UserManager({
    authority: oidcConfig.issuer,
    client_id: oidcConfig.clientId,
    redirect_uri: `${location.origin}/callback`,

    automaticSilentRenew: false,
});

export async function login() {
    console.log("login called");
    try {
        await oidcManager.signinRedirect();
    } catch (error) {
        console.error("Login failed:", error);
        throw error;
    }
}

export function logout() {
    oidcManager.signoutRedirect();
}
