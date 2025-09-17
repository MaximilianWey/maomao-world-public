package net.maomaocloud.authservice.api.auth.ldap;

import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import net.maomaocloud.authservice.api.auth.common.AuthProviderService;
import net.maomaocloud.authservice.api.auth.common.AuthProviderType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LdapAuthService implements AuthProviderService {


    @Override
    public List<AuthProvider> getProviders() {
        return List.of();
    }

    @Override
    public AuthProvider registerProvider(AuthProvider provider) {
        throw new DisabledException("LDAP is not supported yet");
    }

    @Override
    public void deleteProvider(AuthProvider provider) throws IllegalArgumentException {
        throw new DisabledException("LDAP is not supported yet");
    }

    @Override
    public AuthProvider updateProvider(AuthProvider provider) {
        throw new DisabledException("LDAP is not supported yet");
    }

    @Override
    public AuthProviderType managingType() {
        return AuthProviderType.LDAP;
    }

    public Optional<LdapAuthProvider> getProvider(UUID providerId) {
        return Optional.empty();
    }
}
