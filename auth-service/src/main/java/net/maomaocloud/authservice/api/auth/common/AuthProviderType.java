package net.maomaocloud.authservice.api.auth.common;

import net.maomaocloud.authservice.api.auth.common.AuthRequest.*;

public enum AuthProviderType {
    LOCAL,
    OIDC,
    LDAP,
    OAUTH2;

    public boolean support(AuthRequest request) {
        return switch (this) {
            case LOCAL -> request instanceof LocalAuthRequest;
            case OIDC -> request instanceof OidcAuthRequest
                    || request instanceof LinkAccountRequest;
            case LDAP -> request instanceof LdapAuthRequest;
            case OAUTH2 -> request instanceof OAuth2AuthRequest
                    || request instanceof LinkAccountRequest;
        };
    }
}
