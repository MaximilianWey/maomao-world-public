package net.maomaocloud.maomaomusic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtDecoder decoder,
                                                   JwtDecoderConfig config) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(decoder)
                        )
                        .authenticationEntryPoint((req, resp, auth) -> {
                            if (!config.isJwksAvailable()) {
                                resp.setStatus(503);
                                resp.getWriter().write("Authentication service temporarily unavailable");
                            } else {
                                resp.setStatus(401);
                                resp.getWriter().write("Unauthorized");
                            }
                        })
                );
        return http.build();
    }
}