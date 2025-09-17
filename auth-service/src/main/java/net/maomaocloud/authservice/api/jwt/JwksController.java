package net.maomaocloud.authservice.api.jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("public/.well-known")
public class JwksController {

    private final RSAPublicKey publicKey;

    public JwksController(JwtTokenUtil jwtTokenUtil) {
        this.publicKey = (RSAPublicKey) jwtTokenUtil.getPublicKey();
    }

    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        String modulus = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray());
        String exponent = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray());

        Map<String, Object> jwk = Map.of(
                "kty", "RSA",
                "use", "sig",
                "kid", "maomaocloud-key",
                "alg", "RS256",
                "n", modulus,
                "e", exponent
        );

        return Map.of("keys", List.of(jwk));
    }
}
