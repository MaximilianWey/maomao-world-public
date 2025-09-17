package net.maomaocloud.authservice.api.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LinkedAccountRepository extends JpaRepository<LinkedAccount, UUID> {
    Optional<LinkedAccount> findByExternalIdAndProviderId(String externalId, UUID providerId);
}
