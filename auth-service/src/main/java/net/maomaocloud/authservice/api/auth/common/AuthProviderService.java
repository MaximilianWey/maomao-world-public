package net.maomaocloud.authservice.api.auth.common;

import java.util.List;

public interface AuthProviderService {
    List<AuthProvider> getProviders();
    AuthProvider registerProvider(AuthProvider provider);
    void deleteProvider(AuthProvider provider) throws IllegalArgumentException;
    AuthProvider updateProvider(AuthProvider provider);
    AuthProviderType managingType();
}
