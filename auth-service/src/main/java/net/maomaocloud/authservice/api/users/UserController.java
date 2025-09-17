package net.maomaocloud.authservice.api.users;

import dev.openfga.sdk.errors.HttpStatusCode;
import net.maomaocloud.authservice.api.auth.common.AuthRequest;
import net.maomaocloud.authservice.api.auth.common.AuthResult;
import net.maomaocloud.authservice.api.auth.AuthenticationService;
import net.maomaocloud.authservice.api.jwt.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final MaoMaoUserDetailsService userDetailsService;
    private final AuthenticationService authService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserController(UserService userService,
                          MaoMaoUserDetailsService userDetailsService,
                          AuthenticationService authService,
                          JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.authService = authService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        try {
            if (jwtTokenUtil.isTokenExpired(jwt)) {
                return ResponseEntity.status(401).build();
            }

            Map<String, String> profile = jwt.getClaim("profile");
            if (profile == null || profile.isEmpty() || !profile.containsKey("userId")) {
                return ResponseEntity.status(HttpStatusCode.UNAUTHORIZED).build();
            }

            UUID id = profile.get("userId") != null ? UUID.fromString(profile.get("userId")) : null;
            if (id == null) {
                LOGGER.error("Unexpected state - user ID not found in JWT profile");
                return ResponseEntity.status(401).build();
            }
            Optional<UserProfile> userProfile = userService.findUserProfile(id);
            if (userProfile.isEmpty()) {
                LOGGER.error("Unexpected state - user profile not found: {}", id);
                return ResponseEntity.status(401).build();
            }
            return ResponseEntity.ok(userProfile.get());
        } catch (Exception e) {
            LOGGER.error("Failed to get user profile", e);
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/update/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateUserDTO updateUserDTO,
                                           @AuthenticationPrincipal Jwt jwt) {
        if (updateUserDTO == null || jwt == null) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, String> profile = jwt.getClaim("profile");
        if (profile == null || profile.isEmpty() || !profile.containsKey("userId")) {
            return ResponseEntity.status(HttpStatusCode.UNAUTHORIZED).build();
        }

        try {
            UserProfile updatedProfile = userService.updateUserProfile(UUID.fromString(profile.get("userId")), updateUserDTO);
            UserDetails details = userDetailsService.loadUserByUsername(updatedProfile.getUser().getUsername());
            String token = jwtTokenUtil.generateToken(updatedProfile, details);
            return ResponseEntity.ok(Map.of(
                    "userProfile", updatedProfile,
                    "token", token
            ));

        } catch (Exception e) {
            LOGGER.error("Failed to update user profile", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/link/oidc/{providerId}")
    public ResponseEntity<?> link(@PathVariable UUID providerId,
                                  @RequestParam String redirect_uri,
                                  @AuthenticationPrincipal Jwt jwt) throws Exception {
        if (providerId == null || redirect_uri == null || redirect_uri.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid provider ID or redirect URI");
        }

        if (jwt == null) {
            LOGGER.warn("OIDC linking request without authentication");
            return ResponseEntity.status(HttpStatusCode.UNAUTHORIZED).body("User not authenticated");
        }

        AuthRequest.LinkAccountRequest request = AuthRequest.LinkAccountRequest.ofOidc(redirect_uri, jwt);

        AuthResult result = authService.authenticate(providerId, request);

        if (result instanceof AuthResult.RedirectResult(String redirectUri)) {
            LOGGER.info("Returning redirect URL for OIDC linking: {}", redirectUri);
            return ResponseEntity.ok(Map.of("redirectUrl", redirectUri));
        } else {
            return ResponseEntity.status(500)
                    .body("Unexpected result type for linking: " + result.getClass().getSimpleName());
        }
    }

    @GetMapping("/link/oauth2/{providerId}")
    public ResponseEntity<?> linkOAuth2(@PathVariable UUID providerId,
                                        @RequestParam String redirect_uri,
                                        @AuthenticationPrincipal Jwt jwt) throws Exception {
        if (providerId == null || redirect_uri == null || redirect_uri.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid provider ID or redirect URI");
        }

        if (jwt == null) {
            LOGGER.warn("OAuth2 linking request without authentication");
            return ResponseEntity.status(HttpStatusCode.UNAUTHORIZED).body("User not authenticated");
        }

        AuthRequest.LinkAccountRequest request = AuthRequest.LinkAccountRequest.ofOAuth2(redirect_uri, jwt);

        AuthResult result = authService.authenticate(providerId, request);

        if (result instanceof AuthResult.RedirectResult(String redirectUri)) {
            LOGGER.info("Returning redirect URL for OAuth2 linking: {}", redirectUri);
            return ResponseEntity.ok(Map.of("redirectUrl", redirectUri));
        } else {
            return ResponseEntity.status(500)
                    .body("Unexpected result type for linking: " + result.getClass().getSimpleName());
        }
    }

    @GetMapping("/disconnect/{linkId}")
    public ResponseEntity<?> disconnect(@PathVariable UUID linkId,
                                        @AuthenticationPrincipal Jwt jwt) {
        if (linkId == null || jwt == null) {
            return ResponseEntity.badRequest().body("Invalid provider ID or user not authenticated");
        }

        Map<String, String> profile = jwt.getClaim("profile");
        if (profile == null || profile.isEmpty() || !profile.containsKey("userId")) {
            return ResponseEntity.status(HttpStatusCode.UNAUTHORIZED).build();
        }

        try {
            UUID userId = UUID.fromString(profile.get("userId"));
            userService.disconnectProvider(userId, linkId);
            UserProfile userProfile = userService.findUserProfile(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));
            UserDetails details = userDetailsService.loadUserByUsername(userProfile.getUser().getUsername());
            String token = jwtTokenUtil.generateToken(userProfile, details);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userProfile", userProfile
            ));
        } catch (Exception e) {
            LOGGER.error("Failed to disconnect provider", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
