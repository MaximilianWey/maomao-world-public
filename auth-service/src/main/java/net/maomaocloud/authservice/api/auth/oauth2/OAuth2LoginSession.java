package net.maomaocloud.authservice.api.auth.oauth2;

import java.util.UUID;

public class OAuth2LoginSession {

    private final UUID providerId;
    private final String codeVerifier;
    private final long createdAt;
    private final String redirectUri;
    private boolean isLinking;
    private UUID internalUserId;

    public OAuth2LoginSession(UUID providerId, String codeVerifier, String redirectUri) {
        this.providerId = providerId;
        this.codeVerifier = codeVerifier;
        this.createdAt = System.currentTimeMillis();
        this.redirectUri = redirectUri;
        this.isLinking = false;
        this.internalUserId = null;
    }

    public boolean isExpired(long timeoutMs) {
        return System.currentTimeMillis() - createdAt > timeoutMs;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public boolean isLinking() {
        return isLinking;
    }

    public void setLinking(boolean linking) {
        this.isLinking = linking;
    }

    public UUID getInternalUserId() {
        return internalUserId;
    }

    public void setInternalUserId(UUID internalUserId) {
        this.internalUserId = internalUserId;
    }
}
