package net.maomaocloud.authservice.api.auth.common;

import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthRequest {
    AuthProviderType getType();

    record LocalAuthRequest(String identifier, String password) implements AuthRequest {
        @Override
        public AuthProviderType getType() {
            return AuthProviderType.LOCAL;
        }
    }

    record OidcAuthRequest(String redirectUri) implements AuthRequest {
        @Override
        public AuthProviderType getType() {
            return AuthProviderType.OIDC;
        }
    }

    record LdapAuthRequest(String identifier, String password) implements AuthRequest {
        @Override
        public AuthProviderType getType() {
            return AuthProviderType.LDAP;
        }
    }

    record OAuth2AuthRequest(String redirectUri) implements AuthRequest {
        @Override
        public AuthProviderType getType() {
            return AuthProviderType.OAUTH2;
        }
    }

    record LinkAccountRequest(AuthRequest request, String jwt) implements AuthRequest {
        public LinkAccountRequest(AuthRequest request, Jwt jwt) {
            this(request, jwt.getTokenValue());
        }
        public static LinkAccountRequest ofOidc(String redirectUri, Jwt jwt) {
            return new LinkAccountRequest(new OidcAuthRequest(redirectUri), jwt);
        }

        public static LinkAccountRequest ofOAuth2(String redirectUri, Jwt jwt) {
            return new LinkAccountRequest(new OAuth2AuthRequest(redirectUri), jwt);
        }

        @Override
        public AuthProviderType getType() {
            return request.getType();
        }
    }
}
