package net.maomaocloud.authservice.api.auth.common;

public sealed interface AuthResult permits AuthResult.RedirectResult, AuthResult.TokenResult {
    record RedirectResult(String redirectUri) implements AuthResult {}
    record TokenResult(String token) implements AuthResult {}
}
