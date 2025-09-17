package net.maomaocloud.maomaomusic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class JwtDecoderConfig {

    @Value("${jwks.endpoint}")
    private String jwksUrl;

    private final AtomicReference<JwtDecoder> decoderRef = new AtomicReference<>();
    private volatile boolean jwksAvailable = false;

    @Bean
    public JwtDecoder jwtDecoder() {
        refreshDecoder();
        return token -> {
            JwtDecoder decoder = decoderRef.get();
            if (decoder == null) {
                throw new JwtException("JWKS currently unavailable");
            }
            return decoder.decode(token);
        };
    }

    @Scheduled(fixedDelay = 60000)  // Retry every 60 seconds
    public void refreshDecoder() {
        try {
            NimbusJwtDecoder newDecoder = NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
            // Test decoding a dummy JWT to confirm it works (optional but recommended)
            decoderRef.set(newDecoder);
            jwksAvailable = true;
        } catch (Exception e) {
            jwksAvailable = false;
            decoderRef.set(null);
            System.err.println("Failed to refresh JWKS: " + e.getMessage());
        }
    }

    public boolean isJwksAvailable() {
        return jwksAvailable;
    }
}
