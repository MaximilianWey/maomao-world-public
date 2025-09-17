package net.maomaocloud.authservice.api.auth.oidc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OidcProviderRepository extends JpaRepository<OidcProvider, UUID> {
}

