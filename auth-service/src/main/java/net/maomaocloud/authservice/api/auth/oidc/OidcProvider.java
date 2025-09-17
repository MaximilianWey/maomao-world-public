package net.maomaocloud.authservice.api.auth.oidc;

import jakarta.persistence.*;
import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import net.maomaocloud.authservice.api.auth.AuthProviderMetadata;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "oidc_providers", schema = "maomao_auth")
public class OidcProvider implements AuthProvider {

    private static final String SCOPE = "openid profile email";

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id", referencedColumnName = "id", nullable = false)
    private AuthProviderMetadata metadata;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "issuer_uri", nullable = false)
    private String issuerUri;

    @Column(name = "redirect_uri", nullable = false)
    private String redirectUri;

    @Column(name = "authorization_url", nullable = false)
    private String authorizationUri;

    @Column(name = "token_url", nullable = false)
    private String tokenUri;

    @Column(name = "userinfo_url", nullable = false)
    private String userInfoUri;

    @Column(name = "jwks_url", nullable = false)
    private String jwksUri;

    @Column(name = "logout_url")
    private String logoutUri;

    @Column(name = "scope", nullable = false)
    private String scope;

    public OidcProvider() {}

    public OidcProvider(UUID id,
                        AuthProviderMetadata metadata,
                        String clientId,
                        String clientSecret,
                        String issuerUri,
                        String redirectUri,
                        String authorizationUri,
                        String tokenUri,
                        String userInfoUri,
                        String jwksUri,
                        String logoutUri,
                        String scope) {
        this.id = id;
        this.metadata = metadata;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.issuerUri = issuerUri;
        this.redirectUri = redirectUri;
        this.authorizationUri = authorizationUri;
        this.tokenUri = tokenUri;
        this.userInfoUri = userInfoUri;
        this.jwksUri = jwksUri;
        this.logoutUri = logoutUri;
        this.scope = scope != null ? scope : SCOPE;
    }

    @PrePersist
    public void prePersist() {
        if (scope == null || scope.isEmpty()) {
            scope = SCOPE;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public AuthProviderMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AuthProviderMetadata metadata) {
        this.metadata = metadata;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAuthorizationUri() {
        return authorizationUri;
    }

    public void setAuthorizationUri(String authorizationUri) {
        this.authorizationUri = authorizationUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getUserInfoUri() {
        return userInfoUri;
    }

    public void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public String getLogoutUri() {
        return logoutUri;
    }

    public void setLogoutUri(String logoutUri) {
        this.logoutUri = logoutUri;
    }

    public String getScope() {
        return scope != null ? scope : SCOPE;
    }

    public void setScope(String scope) {
        this.scope = scope != null ? scope : SCOPE;
    }
}
