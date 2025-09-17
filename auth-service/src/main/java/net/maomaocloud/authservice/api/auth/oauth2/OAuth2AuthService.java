package net.maomaocloud.authservice.api.auth.oauth2;

import net.maomaocloud.authservice.api.auth.common.*;
import net.maomaocloud.authservice.api.auth.common.AuthRequest.LinkAccountRequest;
import net.maomaocloud.authservice.api.auth.common.AuthRequest.OAuth2AuthRequest;
import net.maomaocloud.authservice.api.auth.common.AuthResult.RedirectResult;
import net.maomaocloud.authservice.api.auth.common.LoginExceptions.AlreadyLinkedException;
import net.maomaocloud.authservice.api.auth.common.LoginExceptions.InsufficientCapabilitiesException;
import net.maomaocloud.authservice.api.auth.common.LoginExceptions.UsernameAlreadyTakenException;
import net.maomaocloud.authservice.api.jwt.JwtTokenUtil;
import net.maomaocloud.authservice.api.users.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.HttpMethod.*;

@Service
public class OAuth2AuthService implements AuthProviderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2AuthService.class);

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final RestTemplate restTemplate;
    private final UserDetailsService userDetailsService;
    private final OAuth2ProviderRepository oauth2ProviderRepository;

    private final Map<String, OAuth2LoginSession> sessions = new ConcurrentHashMap<>();
    private final long SESSION_TIMEOUT_MS = 5 * 60 * 1000;

    @Autowired
    public OAuth2AuthService(UserService userService,
                             JwtTokenUtil jwtTokenUtil,
                             RestTemplate restTemplate,
                             MaoMaoUserDetailsService userDetailsService,
                             OAuth2ProviderRepository oauth2ProviderRepository) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.restTemplate = restTemplate;
        this.userDetailsService = userDetailsService;
        this.oauth2ProviderRepository = oauth2ProviderRepository;
    }

    @Override
    public List<AuthProvider> getProviders() {
        return new ArrayList<>(oauth2ProviderRepository.findAll());
    }

    @Override
    public AuthProvider registerProvider(AuthProvider provider) {
        if (provider instanceof OAuth2Provider oauth2Provider) {
            if (oauth2Provider.getId() != null && oauth2ProviderRepository.existsById(oauth2Provider.getId())) {
                throw new IllegalArgumentException("Provider already exists: " + oauth2Provider.getId());
            }
            return oauth2ProviderRepository.save(oauth2Provider);
        }
        throw new IllegalArgumentException("Provider must be an OAuth2Provider");
    }

    @Override
    public void deleteProvider(AuthProvider provider) throws IllegalArgumentException {
        if (provider instanceof OAuth2Provider oauth2Provider) {
            oauth2ProviderRepository.delete(oauth2Provider);
        } else {
            throw new IllegalArgumentException("Provider must be an OAuth2Provider");
        }
    }

    @Override
    public AuthProvider updateProvider(AuthProvider provider) {
        if (provider instanceof OAuth2Provider oauth2Provider) {
            if (!oauth2ProviderRepository.existsById(oauth2Provider.getId())) {
                throw new IllegalArgumentException("Provider does not exist: " + oauth2Provider.getId());
            }
            return oauth2ProviderRepository.save(oauth2Provider);
        }
        throw new IllegalArgumentException("Provider must be an OAuth2Provider");
    }

    @Override
    public AuthProviderType managingType() {
        return AuthProviderType.OAUTH2;
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanupExpiredSessions() {
        LOGGER.info("Cleaned up expired OAuth2 login sessions. Remaining sessions: {}", sessions.size());
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired(SESSION_TIMEOUT_MS));
    }

    public RedirectResult authenticate(OAuth2Provider provider, OAuth2AuthRequest request) {
        if (!provider.getMetadata().getCapabilityLevel().canLogin()) {
            throw new IllegalStateException("Provider does not support login: " + provider.getId());
        }

        String state = generateState();

        PkceUtils.PkcePair pkce = PkceUtils.generatePkce();

        OAuth2LoginSession session = new OAuth2LoginSession(
                provider.getId(),
                pkce.verifier(),
                request.redirectUri()
        );
        sessions.put(state, session);

        return new RedirectResult(buildAuthorizationUrl(provider, state, pkce.challenge()));
    }

    public RedirectResult linkAccount(OAuth2Provider provider, LinkAccountRequest link) {
        if (!provider.getMetadata().getCapabilityLevel().canLink()) {
            throw new IllegalStateException("Provider does not support account linking: " + provider.getId());
        }

        String username = jwtTokenUtil.getUsername(link.jwt());
        User user = userService.findUser(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        if (link.request() instanceof OAuth2AuthRequest(String redirectUri)) {
            LOGGER.info("Linking account for OAuth2 provider: {}", provider.getMetadata().getProviderName());
            String state = generateState();

            PkceUtils.PkcePair pkce = PkceUtils.generatePkce();

            OAuth2LoginSession session = new OAuth2LoginSession(
                    provider.getId(),
                    pkce.verifier(),
                    redirectUri
            );
            session.setInternalUserId(user.getId());
            session.setLinking(true);

            sessions.put(state, session);
            return new RedirectResult(buildAuthorizationUrl(provider, state, pkce.challenge()));
        }
        throw new IllegalArgumentException("Invalid request type for linking account: " + link.request().getType());
    }

    private String generateState() {
        return UUID.randomUUID().toString();
    }

    private String buildAuthorizationUrl(OAuth2Provider provider, String state, String codeChallenge) {
        return UriComponentsBuilder.fromUriString(provider.getAuthorizationUri())
                .queryParam("client_id", provider.getClientId())
                .queryParam("redirect_uri", provider.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("scope", "identify email")
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .toUriString();
    }

    public Optional<CallbackResult> handleCallback(String code, String state) {
        LOGGER.info("Handling OAuth2 callback with code={} and state={}", code, state);

        OAuth2LoginSession session = getSession(state)
                .orElseThrow(() -> new IllegalStateException("Invalid or expired state: " + state));

        OAuth2Provider provider = oauth2ProviderRepository.findById(session.getProviderId())
                .orElseThrow(() -> new IllegalStateException("Provider not found for ID: " + session.getProviderId()));

        try {
            Map<String, Object> tokenResponse = exchangeCodeForToken(code, provider, session);

            String accessToken = tokenResponse.get("access_token").toString();
            if (accessToken == null || accessToken.isEmpty()) {
                throw new IllegalStateException("Access token is missing in the response");
            }

            return session.isLinking() ?
                    handleLinkCallback(session, accessToken, provider) :
                    handleAuthenticationCallback(session, accessToken, provider);
        } catch (Exception e) {
            LOGGER.error("Error handling OAuth2 callback: {}", e.getMessage(), e);
            return Optional.of(new CallbackResult(
                    null,
                        session.getRedirectUri(),
                        e
                    )
            );
        } finally {
            sessions.remove(state); // Clean up session after processing
        }
    }

    private Optional<CallbackResult> handleLinkCallback(OAuth2LoginSession session,
                                                        String accessToken,
                                                        OAuth2Provider provider)
            throws InsufficientCapabilitiesException, AlreadyLinkedException {
        Map<String, Object> userInfo = getUserInfo(accessToken, provider);

        UserDetails userDetails = linkAccount(userInfo, session, provider);

        var userProfile = userService.findUserProfile(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User profile not found for user: " + userDetails.getUsername()));
        String jwtToken = jwtTokenUtil.generateToken(userProfile, userDetails);

        return Optional.of(new CallbackResult(
                jwtToken,
                session.getRedirectUri(),
                null // no error
        ));
    }

    private UserDetails linkAccount(Map<String, Object> userInfo, OAuth2LoginSession session, OAuth2Provider provider)
            throws AlreadyLinkedException, InsufficientCapabilitiesException {
        if (!provider.getMetadata().getCapabilityLevel().canLink()) {
            throw new InsufficientCapabilitiesException(
                    "This provider does not support account linking. Please contact support if you believe this is an error."
            );
        }

        String id = userInfo.get("id").toString();

        Optional<LinkedAccount> alreadyLinkedProvider = userService
                .findAccountLinkByExternalIdAndProviderId(id, provider.getId());

        if (alreadyLinkedProvider.isPresent()) {
            throw new AlreadyLinkedException(
                    "This account is already linked to another user. Please log in with that account."
            );
        }

        UserProfile profile = userService.findUserProfile(session.getInternalUserId())
                .orElseThrow(() -> new IllegalStateException("User profile not found for user ID: " + session.getInternalUserId()));

        String username = getUsername(userInfo, provider);
        String email = userInfo.containsKey("email")
                ? userInfo.get("email").toString()
                : "";
        String displayName = userInfo.containsKey("display_name")
                ? userInfo.get("display_name").toString()
                : username;
        String avatarUrl = getAvatarUrl(userInfo, provider);

        LinkedAccount linkedAccount = new LinkedAccount(
                profile,
                provider.getId(),
                id,
                provider.getMetadata().getProviderName(),
                username,
                email,
                avatarUrl,
                Instant.now()
        );

        if (displayName != null) {
            linkedAccount.addExtraData("display_name", displayName);
        }

        User updatedUser = userService.linkAccount(profile, linkedAccount);
        return userDetailsService.loadUserByUsername(updatedUser.getUsername());
    }

    private Optional<CallbackResult> handleAuthenticationCallback(OAuth2LoginSession session,
                                                                  String accessToken,
                                                                  OAuth2Provider provider)
            throws InsufficientCapabilitiesException, UsernameAlreadyTakenException {

        Map<String, Object> userInfo = getUserInfo(accessToken, provider);

        UserDetails userDetails = getOrCreateUser(userInfo, provider);

        var userProfile = userService.findUserProfile(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User profile not found for user: " + userDetails.getUsername()));
        String jwtToken = jwtTokenUtil.generateToken(userProfile, userDetails);

        return Optional.of(new CallbackResult(
                jwtToken,
                session.getRedirectUri(),
                null // no error
        ));
    }

    private UserDetails getOrCreateUser(Map<String, Object> userInfo, OAuth2Provider provider) throws UsernameAlreadyTakenException, InsufficientCapabilitiesException {
        String id = userInfo.get("id").toString();
        String username = getUsername(userInfo, provider);
        String email = userInfo.containsKey("email")
                ? userInfo.get("email").toString()
                : "";
        String avatarUrl = getAvatarUrl(userInfo, provider);

        // 1. Check if this account is already linked
        Optional<LinkedAccount> linkedAccount = userService
                .findAccountLinkByExternalIdAndProviderId(id, provider.getId());
        if (linkedAccount.isPresent()) {
            return userDetailsService
                    .loadUserByUsername(linkedAccount.get().getUserProfile().getUser().getUsername());
        }

        // 2. Check if the user already exists
        Optional<UserProfile> existingUser = userService.findUserProfile(username);
        if (existingUser.isPresent()) {
            UserProfile profile = existingUser.get();

            boolean matches = profile.getUser().getProvider().equals(provider.getId())
                    || profile.getLinkedAccounts().stream()
                    .anyMatch(acc -> acc.getProviderId().equals(provider.getId()));

            // 2.1 If the user exists but is not linked to this provider, throw an exception
            if (!matches) {
                throw new UsernameAlreadyTakenException(
                        "An account with this username already exists. If this is you, please log in and link your account."
                );
            }
            // 2.2 Else update their information if necessary
            userService.updateUserProfile(profile.getUserId(), new UpdateUserDTO(
                    username,
                    email,
                    profile.getDisplayName(),
                    avatarUrl
            ));
            return userDetailsService.loadUserByUsername(username);
        }

        // 3. If the user does not exist, create a new user if the provider allows it
        if (provider.getMetadata().getCapabilityLevel().canCreateUser()) {
            Optional<User> newUser = userService.createUser(
                    provider,
                    username,
                    email,
                    null, // No password needed for OAuth2
                    avatarUrl,
                    username
            );
            return newUser
                    .map(User::getUsername)
                    .map(userDetailsService::loadUserByUsername)
                    .orElseThrow(() -> new IllegalStateException("Failed to create user for provider: " + provider.getId()));
        } else {
            throw new InsufficientCapabilitiesException(
                    "This provider does not allow user creation. Please contact support if you believe this is an error."
            );
        }
    }

    private Optional<OAuth2LoginSession> getSession(String state) {
        OAuth2LoginSession session = sessions.get(state);
        if (session == null || session.isExpired(SESSION_TIMEOUT_MS)) {
            sessions.remove(state);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    private Map<String, Object> exchangeCodeForToken(String code,
                                                     OAuth2Provider provider,
                                                     OAuth2LoginSession session) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", provider.getClientId());
        params.add("client_secret", provider.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", provider.getRedirectUri());
        params.add("code_verifier", session.getCodeVerifier());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                provider.getTokenUri(),
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    private Map<String, Object> getUserInfo(String accessToken, OAuth2Provider provider) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                provider.getUserInfoUri(),
                GET,
                request,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    private String getUsername(Map<String, Object> userInfo, OAuth2Provider provider) {
        String fromKey = userInfo.containsKey(provider.getNameKey())
                ? userInfo.get(provider.getNameKey()).toString()
                : "";
        if (!fromKey.isEmpty()) {
            return fromKey;
        }
        if (userInfo.containsKey("preferred_username")) {
            return userInfo.get("preferred_username").toString();
        }
        if (userInfo.containsKey("username")) {
            return userInfo.get("username").toString();
        }
        if (userInfo.containsKey("name")) {
            return userInfo.get("name").toString();
        }
        if (userInfo.containsKey("identifier")) {
            return userInfo.get("identifier").toString();
        }
        if (userInfo.containsKey("login")) {
            return userInfo.get("login").toString();
        }
        LOGGER.warn("No username found in user info: {}", userInfo);
        return userInfo.get("id").toString();
    }

    private String getAvatarUrl(Map<String, Object> userInfo, OAuth2Provider provider) {
        String fromKey = userInfo.containsKey(provider.getAvatarKey())
                ? userInfo.get(provider.getAvatarKey()).toString()
                : "";
        if (!fromKey.isEmpty()) {
            String avatarUrlFormat = provider.getAvatarUrlFormat();
            if (avatarUrlFormat == null || avatarUrlFormat.isEmpty()) {
                return fromKey;
            }
            List<String> keys = extractPlaceHolders(avatarUrlFormat);
            for (String key : keys) {
                String replacement = userInfo.getOrDefault(key, "").toString();
                avatarUrlFormat = avatarUrlFormat.replace("{" + key + "}", replacement);
            }
            return avatarUrlFormat;
        }
        if (userInfo.containsKey("avatar")) {
            return userInfo.get("avatar").toString();
        }
        if (userInfo.containsKey("picture")) {
            return userInfo.get("picture").toString();
        }
        if (userInfo.containsKey("photo")) {
            return userInfo.get("photo").toString();
        }
        if (userInfo.containsKey("profile_picture")) {
            return userInfo.get("profile_picture").toString();
        }
        if (userInfo.containsKey("image")) {
            return userInfo.get("image").toString();
        }
        if (userInfo.containsKey("avatar_url")) {
            return userInfo.get("avatar_url").toString();
        }
        if (userInfo.containsKey("photo_url")) {
            return userInfo.get("photo_url").toString();
        }
        if (userInfo.containsKey("profile_image")) {
            return userInfo.get("profile_image").toString();
        }
        if (userInfo.containsKey("thumbnail")) {
            return userInfo.get("thumbnail").toString();
        }
        if (userInfo.containsKey("icon")) {
            return userInfo.get("icon").toString();
        }
        LOGGER.warn("No avatar URL found in user info: {}", userInfo);
        return null;
    }

    private static List<String> extractPlaceHolders(String format) {
        List<String> keys = new ArrayList<>();
        if (format == null || format.isEmpty()) {
            return keys;
        }
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(format);
        while (matcher.find()) {
            keys.add(matcher.group(1)); // group(1) is the text inside {}
        }
        return keys;
    }
}
