package net.maomaocloud.authservice.api.auth.oauth2;

import jakarta.persistence.*;
import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import net.maomaocloud.authservice.api.auth.AuthProviderMetadata;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "oauth2_providers", schema = "maomao_auth")
public class OAuth2Provider implements AuthProvider {

    private static final String DEFAULT_SCOPE = "identify email";

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

    @Column(name = "authorization_uri", nullable = false)
    private String authorizationUri;

    @Column(name = "token_url", nullable = false)
    private String tokenUri;

    @Column(name = "userinfo_uri", nullable = false)
    private String userInfoUri;

    @Column(name = "redirect_uri", nullable = false)
    private String redirectUri;

    @Column(name = "scope", nullable = false)
    private String scope;

    @Column(name = "avatar_url_key", nullable = false)
    private String avatarKey;

    @Column(name = "avatar_url_format", nullable = false)
    private String avatarUrlFormat;

    @Column(name = "name_key", nullable = false)
    private String nameKey;

    public OAuth2Provider() {
    }

    @PrePersist
    public void prePersist() {
        if (scope == null || scope.isEmpty()) {
            scope = DEFAULT_SCOPE;
        }
        if (avatarKey == null || avatarKey.isEmpty()) {
            avatarKey = "avatar";
        }
        if (nameKey == null || nameKey.isEmpty()) {
            nameKey = "name";
        }
    }

    public UUID getId() {
        return id;
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

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAvatarKey() {
        return avatarKey;
    }

    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
    }

    public String getAvatarUrlFormat() {
        return avatarUrlFormat;
    }

    public void setAvatarUrlFormat(String avatarUrlFormat) {
        this.avatarUrlFormat = avatarUrlFormat;
    }

    public String getNameKey() {
        return nameKey;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }
}
