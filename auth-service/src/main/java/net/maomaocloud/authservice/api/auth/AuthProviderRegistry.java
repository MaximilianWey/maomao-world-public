package net.maomaocloud.authservice.api.auth;

import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import net.maomaocloud.authservice.api.auth.common.AuthProviderCapabilityLevel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class AuthProviderRegistry {

    private final Map<UUID, AuthProvider> providers = new ConcurrentHashMap<>();

    public void registerProvider(AuthProvider provider) {
        providers.put(provider.getMetadata().getId(), provider);
    }

    public Optional<AuthProvider> getProvider(UUID id) {
        return Optional.ofNullable(providers.get(id));
    }

    public List<AuthProviderMetadata> getProviders() {
        return providers.values().stream()
                .map(AuthProvider::getMetadata)
                .toList();
    }

    public List<AuthProviderMetadata> getEnabledProviders() {
        return providers.values().stream()
                .map(AuthProvider::getMetadata)
                .filter(AuthProviderMetadata::isEnabled)
                .toList();
    }

    public List<AuthProviderMetadata> getEnabledProvidersByCapability(AuthProviderCapabilityLevel capability) {
        return providers.values().stream()
                .map(AuthProvider::getMetadata)
                .filter(AuthProviderMetadata::isEnabled)
                .filter(metadata -> metadata.getCapabilityLevel().equals(capability))
                .toList();
    }
}
