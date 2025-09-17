package net.maomaocloud.authservice.api.jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/.well-known")
public class JwtStructureController {

    // TODO : use config file instead of hardcoded logic
    @GetMapping("/jwt-structure.json")
    public Map<String, Object> getJwtStructure() {
        return Map.of(
                "version", "v1.0.0-ALPHA",
                "profile", Map.of(
                        "userId", "string (UUID format)",
                        "displayName", "string",
                        "discordId", "long | null",
                        "avatarUrl", "string | null"
                ),
                "roles", List.of(
                        Map.of("authority", "string")
                ),
                "iat", "integer (issued at timestamp)",
                "exp", "integer (expiration timestamp)"
        );
    }
}