package net.maomaocloud.authservice.api.auth.oidc;

import io.jsonwebtoken.security.Keys;
import net.maomaocloud.authservice.api.auth.common.*;
import net.maomaocloud.authservice.api.auth.common.AuthRequest.LinkAccountRequest;
import net.maomaocloud.authservice.api.auth.common.AuthRequest.OidcAuthRequest;
import net.maomaocloud.authservice.api.auth.common.AuthResult.RedirectResult;
import net.maomaocloud.authservice.api.auth.common.LoginExceptions.AlreadyLinkedException;
import net.maomaocloud.authservice.api.auth.common.LoginExceptions.UsernameAlreadyTakenException;
import net.maomaocloud.authservice.api.jwt.JwtTokenUtil;
import net.maomaocloud.authservice.api.users.LinkedAccount;
import net.maomaocloud.authservice.api.users.User;
import net.maomaocloud.authservice.api.users.UserProfile;
import net.maomaocloud.authservice.api.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableScheduling
public class OidcAuthService implements AuthProviderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthService.class);

    private final long SESSION_TIMEOUT_MS = 5 * 60 * 1000;

    private final UserService userService;
    private final RestTemplate restTemplate;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final OidcProviderRepository providerRepository;

    private final Map<String, OidcLoginSession> sessions;
    private final Map<UUID, JwtDecoder> jwtDecoderCache;

    @Autowired
    public OidcAuthService(UserService userservice,
                           RestTemplate restTemplate,
                           UserDetailsService userDetailsService,
                           JwtTokenUtil jwtTokenUtil,
                           OidcProviderRepository providerRepository) {

        this.userService = userservice;
        this.restTemplate = restTemplate;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.providerRepository = providerRepository;

        this.sessions = new ConcurrentHashMap<>();
        this.jwtDecoderCache = new ConcurrentHashMap<>();
    }

    @Override
    public List<AuthProvider> getProviders() {
        return new ArrayList<>(providerRepository.findAll());
    }

    @Override
    public void deleteProvider(AuthProvider provider) throws IllegalArgumentException {
        if (provider instanceof OidcProvider oidcProvider) {
            if (oidcProvider.getId() == null) {
                throw new IllegalArgumentException("OIDC provider ID cannot be null.");
            }

            if (!providerRepository.existsById(oidcProvider.getId())) {
                throw new IllegalArgumentException("OIDC provider with ID " + oidcProvider.getId() + " does not exist.");
            }

            providerRepository.delete(oidcProvider);
        }
        throw new IllegalArgumentException("Invalid provider type. Expected OidcProvider.");
    }

    @Override
    public AuthProvider registerProvider(AuthProvider provider) {
        if (provider instanceof OidcProvider oidcProvider) {

            if (oidcProvider.getId() != null && providerRepository.existsById(oidcProvider.getId())) {
                throw new IllegalArgumentException("OIDC provider with ID " + oidcProvider.getId() + " already exists.");
            }

            return providerRepository.save(oidcProvider);
        }
        throw new IllegalArgumentException("Invalid provider type. Expected OidcProvider.");
    }

    @Override
    public AuthProvider updateProvider(AuthProvider provider) {
        if (provider instanceof OidcProvider oidcProvider) {
            if (oidcProvider.getId() == null) {
                throw new IllegalArgumentException("OIDC provider and its ID cannot be null.");
            }

            if (!providerRepository.existsById(oidcProvider.getId())) {
                throw new IllegalArgumentException("OIDC provider with ID " + oidcProvider.getId() + " does not exist.");
            }

            return providerRepository.save(oidcProvider);
        }
        throw new IllegalArgumentException("Invalid provider type. Expected OidcProvider.");
    }

    @Override
    public AuthProviderType managingType() {
        return AuthProviderType.OIDC;
    }

    public Optional<OidcProvider> getProvider(UUID id) {
        return providerRepository.findById(id);
    }

    public RedirectResult linkAccount(OidcProvider oidcProvider, LinkAccountRequest link) {
        String username = jwtTokenUtil.getUsername(link.jwt());
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("JWT must contain a valid username claim.");
        }
        Optional<User> userOpt = userService.findUser(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found for username: " + username);
        }
        if (link.request() instanceof OidcAuthRequest(String redirectUri)) {
            LOGGER.info("Linking user {} to OIDC provider {}", username, oidcProvider.getMetadata().getProviderName());
            return new RedirectResult(buildAuthorizationUrl(oidcProvider,
                    redirectUri,
                    userOpt.get().getId(),
                    true)
            );
        }
        throw new IllegalArgumentException("Link request must be of type OidcAuthRequest");
    }

    public RedirectResult authenticate(OidcProvider provider, OidcAuthRequest request) {
        return new RedirectResult(buildAuthorizationUrl(provider, request.redirectUri(), null, false));
    }

    private String buildAuthorizationUrl(OidcProvider provider, String redirectUri, UUID userId, boolean isLinking) {
        String state = generateState();

        PkceUtils.PkcePair pkce = PkceUtils.generatePkce();
        OidcLoginSession session = new OidcLoginSession(provider.getId(), pkce.verifier(), redirectUri);
        session.setInternalUserId(userId);
        session.setLinking(isLinking);
        sessions.put(state, session);

        return UriComponentsBuilder.fromUriString(provider.getAuthorizationUri())
                .queryParam("client_id", provider.getClientId())
                .queryParam("redirect_uri", provider.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("scope", provider.getScope())
                .queryParam("code_challenge", pkce.challenge())
                .queryParam("code_challenge_method", "S256")
                .toUriString();
    }

    private String generateState() {
        return UUID.randomUUID().toString();
    }

    public Optional<CallbackResult> handleCallback(String code, String state) {
        LOGGER.info("Processing callback: code={}, state={}", code, state);

        Map<String, Object> tokens = exchangeCodeForTokens(code, state);

        if (!tokens.containsKey("id_token")) {
            LOGGER.error("ID token missing from OIDC provider response");
            throw new IllegalStateException("ID token missing from OIDC provider response");
        }

        String idToken = tokens.get("id_token").toString();

        OidcLoginSession session = getSession(state);

        Optional<OidcProvider> providerOpt = getProvider(session.getProviderId());

        if (providerOpt.isEmpty()) {
            LOGGER.warn("OIDC provider not found for id: {}", session.getProviderId());
            return Optional.empty();
        }
        OidcProvider provider = providerOpt.get();

        return session.isLinking() ? handleLinkCallback(session, idToken, provider) : handleAuthenticationCallback(session, idToken, provider);
    }

    private Optional<CallbackResult> handleAuthenticationCallback(OidcLoginSession session, String idToken, OidcProvider provider) {
        JwtDecoder decoder = getDecoder(provider);

        try {
            Jwt jwt = decoder.decode(idToken);

            UserDetails userDetails = getOrCreateOidcUser(jwt, provider);
            var userProfileOpt = userService.findUserProfile(userDetails.getUsername());

            String internalJwt = jwtTokenUtil.generateToken(userProfileOpt.get(), userDetails);

            String redirectUri = session.getRedirectUri();
            return Optional.of(new CallbackResult(internalJwt, redirectUri, null));

        } catch (LoginExceptions e) {
            LOGGER.warn("Login exception occurred: {}", e.getMessage());
            return Optional.of(new CallbackResult(null, session.getRedirectUri(), e));
        } catch (UsernameAlreadyTakenException e) {
            LOGGER.warn("Username already taken: {}", e.getMessage());
            return Optional.of(new CallbackResult(null, session.getRedirectUri(), e));
        } catch (IllegalStateException e) {
            LOGGER.error("Illegal state during OIDC authentication: {}", e.getMessage(), e);
            return Optional.of(new CallbackResult(null, session.getRedirectUri(), e));
        } catch (Exception e) {
            LOGGER.error("Failed to process OIDC token: {}", e.getMessage(), e);
            return Optional.of(new CallbackResult(null, session.getRedirectUri(), e));
        }
    }

    private Optional<CallbackResult> handleLinkCallback(OidcLoginSession session, String idToken, OidcProvider provider) {
        JwtDecoder decoder = getDecoder(provider);

        try {
            Jwt jwt = decoder.decode(idToken);

            var userProfileOpt = userService.findUserProfile(session.getInternalUserId());
            if (userProfileOpt.isEmpty()) {
                LOGGER.error("User profile not found for internal user ID: {}", session.getInternalUserId());
                throw new IllegalStateException("User profile not found for internal user ID: " + session.getInternalUserId());
            }
            UserProfile userProfile = userProfileOpt.get();
            UserDetails userDetails = linkAccount(jwt, provider, userProfile);

            String internalJwt = jwtTokenUtil.generateToken(userProfile, userDetails);
            String redirectUri = session.getRedirectUri();

            LOGGER.info("Successfully linked account for user: {}", userProfile.getUser().getUsername());
            return Optional.of(new CallbackResult(internalJwt, redirectUri, null));
        } catch (AlreadyLinkedException e) {
            LOGGER.warn("Account already linked: {}", e.getMessage());
            return Optional.of(new CallbackResult(null, session.getRedirectUri(), e));
        } catch (Exception e) {
            LOGGER.error("Failed to decode ID token for linking: {}", e.getMessage(), e);
            return Optional.of(new CallbackResult(null, session.getRedirectUri(), e));
        }
    }

    private UserDetails linkAccount(Jwt jwt, OidcProvider provider, UserProfile userProfile) throws AlreadyLinkedException {
        var alreadyLinkedProvider = userService.findAccountLinkByExternalIdAndProviderId(
                jwt.getSubject(),
                provider.getId()
        );
        if (alreadyLinkedProvider.isPresent()) {
            throw new AlreadyLinkedException(
                    "This account is already linked to another user. Please log in with that account."
            );
        }

        String subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String displayName = jwt.getClaimAsString("name");
        String username = (preferredUsername != null ? preferredUsername : displayName).toLowerCase();
        String avatarUrl = jwt.getClaimAsString("picture");

        LinkedAccount linkedAccount = new LinkedAccount(
                userProfile,
                provider.getId(),
                subject,
                provider.getMetadata().getProviderName(),
                username,
                email, avatarUrl,
                Instant.now()
        );
        if (displayName != null) {
            linkedAccount.addExtraData("display_name", displayName);
        }

        User updatedUser = userService.linkAccount(userProfile, linkedAccount);
        return userDetailsService.loadUserByUsername(updatedUser.getUsername());
    }

    private JwtDecoder getDecoder(OidcProvider provider) {
        return jwtDecoderCache.computeIfAbsent(provider.getId(), id -> {
            try {
                LOGGER.debug("Fetching JWKS from: {}", provider.getJwksUri());

                ResponseEntity<String> jwksResponse = restTemplate.getForEntity(provider.getJwksUri(), String.class);
                LOGGER.debug("JWKS response status: {}", jwksResponse.getStatusCode());
                LOGGER.debug("JWKS content: {}", jwksResponse.getBody());

                String jwksContent = jwksResponse.getBody();
                if (jwksContent == null || jwksContent.equals("{}") || jwksContent.isBlank()) {
                    LOGGER.warn("JWKS endpoint returned empty or invalid content. Falling back to secret key verification.");

                    return NimbusJwtDecoder.withSecretKey(Keys.hmacShaKeyFor(
                                    provider.getClientSecret().getBytes()))
                            .build();
                }

                return NimbusJwtDecoder.withJwkSetUri(provider.getJwksUri())
                        .jwsAlgorithms(algorithms -> {
                            algorithms.add(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256);
                            algorithms.add(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS384);
                            algorithms.add(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS512);
                            algorithms.add(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.ES256);
                            algorithms.add(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.ES384);
                            algorithms.add(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.ES512);
                        })
                        .build();
            } catch (Exception e) {
                LOGGER.error("Failed to create JWT decoder using JWKS: {}", e.getMessage(), e);
                LOGGER.warn("Attempting fallback to secret key verification");

                try {
                    return NimbusJwtDecoder.withSecretKey(Keys.hmacShaKeyFor(
                                    provider.getClientSecret().getBytes()))
                            .build();
                } catch (Exception e2) {
                    LOGGER.error("Fallback JWT decoder creation also failed: {}", e2.getMessage(), e2);
                    throw new IllegalStateException("Failed to create JWT decoder (both primary and fallback methods failed)", e2);
                }
            }
        });
    }

    private OidcLoginSession getSession(String state) {
        OidcLoginSession session = sessions.get(state);
        if (session == null || session.isExpired(SESSION_TIMEOUT_MS)) {
            sessions.remove(state);
            throw new IllegalStateException("OIDC session expired or missing.");
        }
        return session;

    }

    private Map<String, Object> exchangeCodeForTokens(String code, String state) {
        OidcProvider provider = getOidcProviderFromState(state);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", provider.getRedirectUri());
        params.add("client_id", provider.getClientId());
        params.add("client_secret", provider.getClientSecret());
        params.add("code_verifier", getCodeVerifierForState(state));

        HttpEntity<?> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                provider.getTokenUri(),
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    private OidcProvider getOidcProviderFromState(String state) {
        OidcLoginSession session = sessions.get(state);
        if (session == null || session.isExpired(SESSION_TIMEOUT_MS)) {
            sessions.remove(state);
            throw new IllegalStateException("OIDC session expired or missing.");
        }
        return getProvider((session.getProviderId()))
                .orElseThrow(() -> new IllegalStateException("OIDC provider not found."));
    }

    private String getCodeVerifierForState(String state) {
        OidcLoginSession session = sessions.get(state);
        if (session == null || session.isExpired(SESSION_TIMEOUT_MS)) {
            sessions.remove(state);
            throw new IllegalStateException("OIDC session expired or missing.");
        }
        return session.getCodeVerifier();
    }

    @Scheduled(fixedDelay = 60_000) // check every minute
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(e -> e.getValue().isExpired(SESSION_TIMEOUT_MS));
    }

    public void saveProvider(OidcProvider newProvider) {
        if (newProvider == null) {
            throw new IllegalArgumentException("OIDC provider cannot be null.");
        }
        providerRepository.save(newProvider);
        LOGGER.info("Saved OIDC provider: {}", newProvider.getMetadata().getProviderName());
    }

    private UserDetails getOrCreateOidcUser(Jwt jwt, OidcProvider provider) throws UsernameAlreadyTakenException {
        String subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String displayName = jwt.getClaimAsString("name");
        String avatarUrl = jwt.getClaimAsString("picture");

        LOGGER.debug("OIDC claims received - subject: {}, email: {}, username: {}, name: {}",
                subject, email, preferredUsername, displayName);

        String username = (preferredUsername != null ? preferredUsername : subject).toLowerCase();

        if (email == null) {
            throw new IllegalStateException("Email claim is required but was not provided by OIDC provider");
        }

        // 1. Check if account link exists and login
        Optional<LinkedAccount> linkedAccount = userService.findAccountLinkByExternalIdAndProviderId(subject, provider.getId());
        if (linkedAccount.isPresent()) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(
                    linkedAccount.get().getUserProfile().getUser().getUsername()
            );
            LOGGER.info("Found existing OIDC user for subject: {}", subject);
            return userDetails;
        }

        // 2. Check if user already exists with the same username
        Optional<UserProfile> userProfileOpt = userService.findUserProfile(username);
        if (userProfileOpt.isPresent()) {
            UserProfile userProfile = userProfileOpt.get();

            boolean matches = userProfile.getUser().getProvider().equals(provider.getId())
                    || userProfile.getLinkedAccounts().stream()
                    .anyMatch(acc -> acc.getProviderId().equals(provider.getId()));

            // 2.1 If the user exists but is not linked to this provider, throw an exception
            if (!matches) {
                throw new UsernameAlreadyTakenException(
                        "An account with this username already exists. If this is you, please log in and link your account."
                );
            }

            // 2.2 If the user exists and is linked to this provider, return their UserDetails
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                LOGGER.info("Found existing OIDC user for user: {}", username);
                return userDetails;
            } catch (UsernameNotFoundException e) {
                LOGGER.warn("UserDetails not found for existing profile: {}", username);
                throw e;
            }
        }

        // No existing subject, but check if a user already exists with the same email
        Optional<UserProfile> existingEmailProfile = userService.findUserProfile(email);
        if (existingEmailProfile.isPresent()) {
            LOGGER.warn("Email already in use by another account: {}", email);
            throw new UsernameAlreadyTakenException(
                    "An account with this email already exists. If this is you, please log in and link your account."
            );
        }

        LOGGER.info("Creating new OIDC user for subject: {}", username);
        Optional<User> newUser = userService.createUser(
                provider,
                username,
                email,
                null,
                avatarUrl,
                displayName != null ? displayName : preferredUsername
        );

        return newUser
                .map(user -> userDetailsService.loadUserByUsername(username))
                .orElseThrow(() -> new IllegalStateException("Failed to create OIDC user: " + username));
    }

}
