package net.maomaocloud.authservice.api.auth.ldap;

import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import net.maomaocloud.authservice.api.auth.AuthProviderMetadata;

public class LdapAuthProvider implements AuthProvider {

    @Override
    public AuthProviderMetadata getMetadata() {
        return null;
    }
}
