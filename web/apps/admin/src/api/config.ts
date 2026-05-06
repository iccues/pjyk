import { publicClient } from "./request";

export interface OidcConfig {
  issuer: string;
  clientId: string;
}

export async function getOidcConfig(): Promise<OidcConfig> {
  const request = await publicClient.get<OidcConfig>("/api/config/oidc");
  return request.data;
}
