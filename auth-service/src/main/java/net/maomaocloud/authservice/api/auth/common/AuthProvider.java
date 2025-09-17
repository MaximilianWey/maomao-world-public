package net.maomaocloud.authservice.api.auth.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.maomaocloud.authservice.api.auth.AuthProviderMetadata;
import net.maomaocloud.authservice.api.auth.ldap.LdapAuthProvider;
import net.maomaocloud.authservice.api.auth.local.LocalProvider;
import net.maomaocloud.authservice.api.auth.oauth2.OAuth2Provider;
import net.maomaocloud.authservice.api.auth.oidc.OidcProvider;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LocalProvider.class, name = "LOCAL"),
        @JsonSubTypes.Type(value = OidcProvider.class, name = "OIDC"),
        @JsonSubTypes.Type(value = LdapAuthProvider.class, name = "LDAP"),
        @JsonSubTypes.Type(value = OAuth2Provider.class, name = "OAUTH2"),
})
public interface AuthProvider {
    AuthProviderMetadata getMetadata();
}
