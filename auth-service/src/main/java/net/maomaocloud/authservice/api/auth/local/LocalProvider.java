package net.maomaocloud.authservice.api.auth.local;

import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import net.maomaocloud.authservice.api.auth.AuthProviderMetadata;
import net.maomaocloud.authservice.api.auth.AuthProviderMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static net.maomaocloud.authservice.api.auth.common.AuthProviderCapabilityLevel.USER_SOURCE;
import static net.maomaocloud.authservice.api.auth.common.AuthProviderType.LOCAL;

@Component
public final class LocalProvider implements AuthProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProvider.class);

    private final AuthProviderMetadata METADATA = new AuthProviderMetadata(
            LOCAL,
            "Internal",
            "",
            0,
            true,
            USER_SOURCE
    );

    private final AuthProviderMetadataRepository metadataRepository;
    private final AuthProviderMetadata metadata;

    @Autowired
    public LocalProvider(AuthProviderMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.metadata = initialize();
    }

    @Override
    public AuthProviderMetadata getMetadata() {
        return metadata;
    }

    public AuthProviderMetadata initialize() {
        Optional<AuthProviderMetadata> metadata = metadataRepository.findByType(LOCAL);
        if (metadata.isPresent()) {
            LOGGER.info("Local Auth Provider found, skipping initialization...");
            return metadata.get();
        } else {
            LOGGER.info("No local Auth Provider found, initializing...");
            return metadataRepository.save(METADATA);
        }
    }
}
