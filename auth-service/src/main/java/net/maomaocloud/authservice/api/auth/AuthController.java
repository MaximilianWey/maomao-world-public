package net.maomaocloud.authservice.api.auth;

import jakarta.servlet.http.HttpServletResponse;
import net.maomaocloud.authservice.api.auth.common.AuthRequest.LocalAuthRequest;
import net.maomaocloud.authservice.api.auth.common.AuthRequest.OAuth2AuthRequest;
import net.maomaocloud.authservice.api.auth.common.AuthRequest.OidcAuthRequest;
import net.maomaocloud.authservice.api.auth.common.AuthResult;
import net.maomaocloud.authservice.api.auth.common.AuthResult.RedirectResult;
import net.maomaocloud.authservice.api.auth.common.CallbackResult;
import net.maomaocloud.authservice.api.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authService;
    private final AuthProviderRegistry registry;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationService authService,
                          AuthProviderRegistry registry,
                          UserService userService) {
        this.authService = authService;
        this.registry = registry;
        this.userService = userService;
    }

    @GetMapping("/user/{identifier}")
    public ResponseEntity<?> user(@PathVariable String identifier) {
        var userOpt = userService.findUser(identifier);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("exists", "false"));
        }

        var user = userOpt.get();

        return authService.getRedirectUrlForUser(user.getProvider())
                .map(url -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(url))
                        .build())
                .orElseGet(() -> ResponseEntity.ok(Map.of("exists", "true")));
    }

    @GetMapping("/providers")
    public ResponseEntity<List<AuthProviderMetadata>> providers() {
        return ResponseEntity.ok(registry.getProviders());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LocalAuthRequest request) throws Exception {

        if (request == null) {
            return ResponseEntity.badRequest().body(null);
        }

        AuthResult result = authService.authenticate(request);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/login/oidc/{providerId}")
    public void loginOidc(HttpServletResponse response,
                      @PathVariable UUID providerId,
                      @RequestParam String redirect_uri) throws Exception {

        if (providerId == null || redirect_uri == null || redirect_uri.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid provider ID or redirect URI");
            return;
        }

        AuthResult result = authService.authenticate(providerId, new OidcAuthRequest(redirect_uri));

        if (result instanceof RedirectResult(String redirectUri)) {
            response.sendRedirect(redirectUri);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid authentication result");
        }
    }

    @GetMapping("/login/oauth2/{providerId}")
    public void loginOAuth2(HttpServletResponse response,
                      @PathVariable UUID providerId,
                      @RequestParam String redirect_uri) throws Exception {

        if (providerId == null || redirect_uri == null || redirect_uri.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid provider ID or redirect URI");
            return;
        }

        AuthResult result = authService.authenticate(providerId, new OAuth2AuthRequest(redirect_uri));

        if (result instanceof RedirectResult(String redirectUri)) {
            response.sendRedirect(redirectUri);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid authentication result");
        }
    }

    @GetMapping("/oidc/callback")
    public void oidcCallback(HttpServletResponse response,
                             @RequestParam String code,
                             @RequestParam String state) throws IOException {
        LOGGER.info("OIDC callback received with code={} and state={}", code, state);
        if (code == null || state == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing code or state parameters");
            return;
        }

        try {
            Optional<CallbackResult> resultOpt = authService.handleOidcCallback(code, state);
            if (resultOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid OIDC callback result");
                return;
            }
            CallbackResult result = resultOpt.get();
            if (result.error() != null) {
                String separator = result.redirectUri().contains("?") ? "&" : "?";
                String redirectWithError = result.redirectUri() + separator + "error=" + result.error().getMessage();
                response.sendRedirect(redirectWithError);
                return;
            }

            String jwtToken = result.jwt();
            String redirectUri = result.redirectUri();
            String separator = redirectUri.contains("?") ? "&" : "?";
            String redirectWithToken = redirectUri + separator + "token=" + jwtToken;

            response.sendRedirect(redirectWithToken);
        } catch (Exception e) {
            LOGGER.error("OIDC callback processing failed", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OIDC callback failed");
        }
    }

    @GetMapping("/oauth2/callback")
    public void oauth2Callback(HttpServletResponse response,
                               @RequestParam String code,
                               @RequestParam String state) throws IOException {
        LOGGER.info("OAuth2 callback received with code={} and state={}", code, state);
        if (code == null || state == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing code or state parameters");
            return;
        }

        try {
            Optional<CallbackResult> resultOpt = authService.handleOAuth2Callback(code, state);
            if (resultOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid OAuth2 callback result");
                return;
            }
            CallbackResult result = resultOpt.get();
            if (result.error() != null) {
                String separator = result.redirectUri().contains("?") ? "&" : "?";
                String redirectWithError = result.redirectUri() + separator + "error=" + result.error().getMessage();
                response.sendRedirect(redirectWithError);
                return;
            }

            String jwtToken = result.jwt();
            String redirectUri = result.redirectUri();
            String separator = redirectUri.contains("?") ? "&" : "?";
            String redirectWithToken = redirectUri + separator + "token=" + jwtToken;

            response.sendRedirect(redirectWithToken);
        } catch (Exception e) {
            LOGGER.error("OAuth2 callback processing failed", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2 callback failed");
        }
    }
}