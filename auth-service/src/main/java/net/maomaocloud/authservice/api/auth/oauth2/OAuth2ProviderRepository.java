package net.maomaocloud.authservice.api.auth.oauth2;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OAuth2ProviderRepository extends JpaRepository<OAuth2Provider, UUID> {
}
