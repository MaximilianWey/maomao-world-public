package net.maomaocloud.authservice.api.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import net.maomaocloud.authservice.api.users.LinkedAccountDTO;
import net.maomaocloud.authservice.api.users.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

@Component
public class JwtTokenUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);
    private static final long VALIDITY_IN_MILLISECONDS = 1000 * 60 * 60 * 24; // one day

    @Value("${jwt.key.private.path}")
    private String privateKeyPath;

    @Value("${jwt.key.public.path}")
    private String publicKeyPath;

    private final ResourceLoader resourceLoader;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Autowired
    public JwtTokenUtil(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            Resource privateKeyResource = resourceLoader.getResource(privateKeyPath);
            String privateKeyPem = new String(privateKeyResource.getInputStream().readAllBytes())
                    .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);

            Resource publicKeyResource = resourceLoader.getResource(publicKeyPath);
            String publicKeyPem = new String(publicKeyResource.getInputStream().readAllBytes())
                    .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPem);
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = KeyFactory.getInstance("RSA").generatePublic(pubKeySpec);
        } catch (Exception e) {
            LOGGER.error("Failed to load RSA keys", e);
            throw new IllegalStateException("Could not load RSA keys", e);
        }
    }

    public String generateToken(UserProfile userProfile, UserDetails details) {
        Claims claims = Jwts.claims().setSubject(userProfile.getUser().getUsername());

        List<LinkedAccountDTO> linkedAccounts = userProfile.getLinkedAccounts().stream()
                .map(LinkedAccountDTO::new)
                .toList();

        Map<String, Object> profile = Map.of(
                "userId", userProfile.getUserId().toString(),
                "displayName", userProfile.getDisplayName(),
                "linkedAccounts", linkedAccounts,
                "avatarUrl", userProfile.getAvatarUrl()
        );

        claims.put("profile", profile);

        claims.put("roles", details.getAuthorities());

        Date now = new Date();
        Date validity = new Date(now.getTime() + VALIDITY_IN_MILLISECONDS);

        LOGGER.info("Issued internal JWT for user: {}", userProfile.getUser().getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }


    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenExpired(Jwt jwt) {
        return isTokenExpired(jwt.getTokenValue());
    }

    public boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            LOGGER.warn("Unable to get expiration date from token: {}", e.getMessage());
            return true;
        }
    }

    public Authentication getAuthentication(String token, UserDetails details) {
        return new UsernamePasswordAuthenticationToken(createJwt(token), "", details.getAuthorities());
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public Jwt createJwt(String token) {
        Claims claims = getClaims(token);

        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        Instant issuedAt = claims.getIssuedAt().toInstant();
        Instant expiresAt = claims.getExpiration().toInstant();

        return new Jwt(
                token,
                issuedAt,
                expiresAt,
                headers,
                claims
        );
    }
}