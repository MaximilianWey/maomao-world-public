package net.maomaocloud.authservice.api.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "linked_accounts", schema = "maomao_auth")
public class LinkedAccount {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "provider_id", updatable = false, nullable = false)
    private UUID providerId;

    @Column(name = "provider_name", updatable = false, nullable = false)
    private String providerName;

    @Column(name = "external_id", updatable = false, nullable = false)
    private String externalId;

    @Column(name = "preferred_name", nullable = false)
    private String preferredName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "email")
    private String email;

    @Column(name = "linked_at", updatable = false, nullable = false)
    private Instant linkedAt;

    @Lob
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_data")
    private Map<String, Object> extraData = new HashMap<>();

    public LinkedAccount() {}

    public LinkedAccount(UserProfile userProfile,
                         UUID providerId,
                         String externalId,
                         String providerName,
                         String preferredName,
                         String email, String avatarUrl,
                         Instant linkedAt) {
        this.userProfile = userProfile;
        this.providerId = providerId;
        this.externalId = externalId;
        this.providerName = providerName;
        this.preferredName = preferredName;
        this.avatarUrl = avatarUrl;
        this.linkedAt = linkedAt;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(Instant linkedAt) {
        this.linkedAt = linkedAt;
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void addExtraData(String key, Object value) {
        if (key != null && value != null) {
            this.extraData.put(key, value);
        }
    }

    public void removeExtraData(String key) {
        if (key != null) {
            this.extraData.remove(key);
        }
    }
}
