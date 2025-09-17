package net.maomaocloud.authservice.api.admin;

import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import net.maomaocloud.authservice.api.auth.ldap.LdapAuthProvider;
import net.maomaocloud.authservice.api.auth.ldap.LdapAuthService;
import net.maomaocloud.authservice.api.auth.local.LocalAuthService;
import net.maomaocloud.authservice.api.auth.oidc.OidcAuthService;
import net.maomaocloud.authservice.api.auth.oidc.OidcProvider;
import net.maomaocloud.authservice.api.users.User;
import net.maomaocloud.authservice.api.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {

    private final LocalAuthService localAuthService;
    private final OidcAuthService oidcAuthService;
    private final LdapAuthService ldapAuthService;
    private final UserService userService;

    @Autowired
    public AdminService(LocalAuthService localAuthService,
                        OidcAuthService oidcAuthService,
                        LdapAuthService ldapAuthService,
                        UserService userService) {
        this.localAuthService = localAuthService;
        this.oidcAuthService = oidcAuthService;
        this.ldapAuthService = ldapAuthService;
        this.userService = userService;
    }

    protected AuthProvider registerAuthProvider(OidcProvider provider) {
        return oidcAuthService.registerProvider(provider);
    }

    protected AuthProvider updateAuthProvider(OidcProvider provider) {
        return oidcAuthService.updateProvider(provider);
    }

    protected void deleteAuthProvider(UUID providerId) {
        Optional<OidcProvider> oidcProvider = oidcAuthService.getProvider(providerId);
        if (oidcProvider.isPresent()) {
            deleteOidcProvider(oidcProvider.get());
            return;
        }
        Optional<LdapAuthProvider> ldapProvider = ldapAuthService.getProvider(providerId);
        if (ldapProvider.isPresent()) {
            ldapAuthService.deleteProvider(ldapProvider.get());
            return;
        }
        if (localAuthService.getAuthProvider().getMetadata().getId().equals(providerId)) {
            localAuthService.deleteProvider(localAuthService.getAuthProvider());
        }
        throw new IllegalArgumentException("Auth provider not found: " + providerId);
    }

    private void deleteOidcProvider(OidcProvider provider) {
        oidcAuthService.deleteProvider(provider);
    }

    protected List<User> getUsers() {
        return userService.getUsers();
    }
}
