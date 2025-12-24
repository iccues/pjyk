import { get } from "./http";

export interface OidcConfig {
    issuer: string;
    clientId: string;
}

export async function getOidcConfig(): Promise<OidcConfig> {
    return get<OidcConfig>('/api/config/oidc');
}
