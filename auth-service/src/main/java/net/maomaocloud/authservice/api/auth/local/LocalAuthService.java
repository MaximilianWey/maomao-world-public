package net.maomaocloud.authservice.api.auth.local;

import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import net.maomaocloud.authservice.api.auth.common.AuthProviderService;
import net.maomaocloud.authservice.api.auth.common.AuthProviderType;
import net.maomaocloud.authservice.api.auth.common.AuthRequest.LocalAuthRequest;
import net.maomaocloud.authservice.api.auth.common.AuthResult.TokenResult;
import net.maomaocloud.authservice.api.jwt.JwtTokenUtil;
import net.maomaocloud.authservice.api.users.UserProfile;
import net.maomaocloud.authservice.api.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalAuthService implements AuthProviderService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final LocalProvider authProvider;

    @Autowired
    public LocalAuthService(AuthenticationManager authenticationManager,
                            UserDetailsService userDetailsService,
                            JwtTokenUtil jwtTokenUtil,
                            UserService userService,
                            LocalProvider localAuthProvider) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.authProvider = localAuthProvider;
    }

    @Override
    public List<AuthProvider> getProviders() {
        return List.of(getAuthProvider());
    }

    @Override
    public AuthProvider registerProvider(AuthProvider provider) {
        throw new IllegalArgumentException("Local Auth Provider cannot be registered");
    }

    @Override
    public void deleteProvider(AuthProvider provider) throws IllegalArgumentException {
        throw new IllegalArgumentException("Local Auth Provider cannot be deleted");
    }

    @Override
    public AuthProvider updateProvider(AuthProvider provider) {
        throw new IllegalArgumentException("Local Auth Provider cannot be deleted");
    }

    @Override
    public AuthProviderType managingType() {
        return AuthProviderType.LOCAL;
    }

    public LocalProvider getAuthProvider() {
        return authProvider;
    }

    public TokenResult authenticate(LocalAuthRequest request) throws Exception {
        this.authenticate(request.identifier(), request.password());
        return buildTokenResult(request.identifier());
    }

    private void authenticate(String identifier, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(identifier, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    private TokenResult buildTokenResult(String identifier) {
        UserProfile profile = userService.findUserProfile(identifier)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for identifier: " + identifier));
        UserDetails details = userDetailsService.loadUserByUsername(identifier);
        return new TokenResult(jwtTokenUtil.generateToken(profile, details));
    }
}
