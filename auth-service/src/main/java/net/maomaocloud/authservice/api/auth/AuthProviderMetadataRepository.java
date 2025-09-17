package net.maomaocloud.authservice.api.auth;

import net.maomaocloud.authservice.api.auth.common.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthProviderMetadataRepository extends JpaRepository<AuthProviderMetadata, UUID> {
    Optional<AuthProviderMetadata> findByType(AuthProviderType type);
    boolean existsByType(AuthProviderType type);
    Optional<AuthProviderMetadata> findAuthProviderMetadataByProviderName(String providerName);
}
