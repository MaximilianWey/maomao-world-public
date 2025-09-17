package net.maomaocloud.authservice.api.auth;

import net.maomaocloud.authservice.api.auth.common.*;
import net.maomaocloud.authservice.api.auth.common.AuthRequest.*;
import net.maomaocloud.authservice.api.auth.common.LoginExceptions.DisabledProviderException;
import net.maomaocloud.authservice.api.auth.ldap.LdapAuthProvider;
import net.maomaocloud.authservice.api.auth.local.LocalAuthService;
import net.maomaocloud.authservice.api.auth.local.LocalProvider;
import net.maomaocloud.authservice.api.auth.oauth2.OAuth2AuthService;
import net.maomaocloud.authservice.api.auth.oauth2.OAuth2Provider;
import net.maomaocloud.authservice.api.auth.oidc.OidcAuthService;
import net.maomaocloud.authservice.api.auth.oidc.OidcProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final AuthProviderRegistry registry;
    private final LocalAuthService localAuthService;
    private final OidcAuthService oidcAuthService;
    private final OAuth2AuthService oauth2AuthService;
    private final AuthProviderMetadataRepository authProviderMetadataRepository;

    @Autowired
    public AuthenticationService(AuthProviderRegistry registry,
                                 LocalAuthService localAuthService,
                                 OidcAuthService oidcAuthService,
                                 OAuth2AuthService oauth2AuthService,
                                 AuthProviderMetadataRepository authProviderMetadataRepository) {
        this.registry = registry;
        this.localAuthService = localAuthService;
        this.oidcAuthService = oidcAuthService;
        this.oauth2AuthService = oauth2AuthService;
        this.authProviderMetadataRepository = authProviderMetadataRepository;
    }

    public AuthResult authenticate(LocalAuthRequest request) throws Exception {
        return localAuthService.authenticate(request);
    }

    public AuthResult authenticate(UUID providerId, AuthRequest request) throws Exception {
        AuthProvider provider = registry.getProvider(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found: " + providerId));

        AuthProviderType type = provider.getMetadata().getType();

        if (!provider.getMetadata().isEnabled()) {
            throw new DisabledProviderException("Provider is disabled: " + type);
        }

        if (!type.support(request)) {
            throw new IllegalArgumentException("Auth request type " + request.getType() + " does not match provider type " + type);
        }

        return switch (request) {
            case LocalAuthRequest local when provider instanceof LocalProvider localProvider
                    -> localAuthService.authenticate(local);
            case LdapAuthRequest ldap when provider instanceof LdapAuthProvider ldapProvider
                    -> throw new RuntimeException("LDAP authentication is not yet supported");
            case OidcAuthRequest oidc when provider instanceof OidcProvider oidcProvider
                    -> oidcAuthService.authenticate(oidcProvider, oidc);
            case LinkAccountRequest link when provider instanceof OidcProvider oidcProvider
                    -> oidcAuthService.linkAccount(oidcProvider, link);
            case OAuth2AuthRequest oauth2 when provider instanceof OAuth2Provider oauth2Provider
                    -> oauth2AuthService.authenticate(oauth2Provider, oauth2);
            case LinkAccountRequest link when provider instanceof OAuth2Provider oauth2Provider
                    -> oauth2AuthService.linkAccount(oauth2Provider, link);
            default -> throw new IllegalArgumentException("Unsupported auth request type: " + request.getType());
        };
    }

    public Optional<String> getRedirectUrlForUser(UUID providerId) {
        return registry.getProvider(providerId).flatMap(authProvider -> switch (authProvider) {
            case OidcProvider oidcProvider -> Optional.of(oidcProvider.getAuthorizationUri());
            case OAuth2Provider oauth2Provider -> Optional.of(oauth2Provider.getAuthorizationUri());
            default -> Optional.empty();
        });
    }

    public Optional<CallbackResult> handleOidcCallback(String code, String state) {
        return oidcAuthService.handleCallback(code, state);
    }

    public Optional<CallbackResult> handleOAuth2Callback(String code, String state) {
        return oauth2AuthService.handleCallback(code, state);
    }

    public Optional<AuthProviderMetadata> getProviderMetadataByName(String providerName) {
        return authProviderMetadataRepository.findAuthProviderMetadataByProviderName(providerName);
    }
}
