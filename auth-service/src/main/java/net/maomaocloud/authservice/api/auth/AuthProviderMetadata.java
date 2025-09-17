package net.maomaocloud.authservice.api.auth;

import jakarta.persistence.*;
import net.maomaocloud.authservice.api.auth.common.AuthProviderCapabilityLevel;
import net.maomaocloud.authservice.api.auth.common.AuthProviderType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "auth_provider_metadata", schema = "maomao_auth")
public class AuthProviderMetadata {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private AuthProviderType type;

    @Column(name = "provider_name", nullable = false, unique = true)
    private String providerName;

    @Column(name = "logo_url", nullable = false)
    private String logoUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "capability_level")
    private AuthProviderCapabilityLevel capabilityLevel;

    @PrePersist
    private void prePersist() {
        if (displayOrder == null) {
            displayOrder = 10;
        }
        if (isEnabled == null) {
            isEnabled = true;
        }
        if (capabilityLevel == null) {
            capabilityLevel = AuthProviderCapabilityLevel.LOGIN_ONLY;
        }
    }

    public AuthProviderMetadata() {}

    public AuthProviderMetadata(AuthProviderType type, String displayName, String logoUrl, Integer displayOrder, Boolean isEnabled, AuthProviderCapabilityLevel capabilityLevel) {
        this.type = type;
        this.providerName = displayName;
        this.logoUrl = logoUrl;
        this.displayOrder = displayOrder;
        this.isEnabled = isEnabled;
        this.capabilityLevel = capabilityLevel;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AuthProviderType getType() {
        return type;
    }

    public void setType(AuthProviderType type) {
        this.type = type;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String displayName) {
        this.providerName = displayName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public AuthProviderCapabilityLevel getCapabilityLevel() {
        return capabilityLevel;
    }

    public void setCapabilityLevel(AuthProviderCapabilityLevel capabilityLevel) {
        this.capabilityLevel = capabilityLevel;
    }

}
