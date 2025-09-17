package net.maomaocloud.authservice.api.auth.common;

public record CallbackResult(String jwt,
                             String redirectUri,
                             Throwable error) {
}
