export interface AuthProviderMetadata {
    id: string;
    type: AuthProviderType;
    providerName: string;
    logoUrl: string;
    displayOrder: number;
    capabilityLevel: AuthProviderCapabilityLevel;
    enabled: boolean;
}

export type AuthProviderType = 'LOCAL' | 'OIDC' | 'LDAP' | 'SAML' | 'OAUTH2'

export type AuthProviderCapabilityLevel = 'USER_SOURCE' | 'LOGIN_ONLY' | 'LINK_ONLY'