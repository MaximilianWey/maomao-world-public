package net.maomaocloud.authservice.api.auth.common;

import javax.security.auth.login.LoginException;

public class LoginExceptions extends RuntimeException {

    public LoginExceptions(String message) {
        super(message);
    }

    public static class UnknownProviderException extends LoginException {
        public UnknownProviderException(String message) {
            super(message);
        }
    }
    public static class UnknownOidcProviderException extends UnknownProviderException {
        public UnknownOidcProviderException(String message) {
            super(message);
        }
    }
    public static class UnknownLdapProviderException extends UnknownProviderException {
        public UnknownLdapProviderException(String message) {
            super(message);
        }
    }
    public static class DisabledProviderException extends LoginException {
        public DisabledProviderException(String message) {
            super(message);
        }
    }
    public static class InsufficientCapabilitiesException extends LoginException {
        public InsufficientCapabilitiesException(String message) {
            super(message);
        }
    }
    public static class AlreadyLinkedException extends LoginException {
        public AlreadyLinkedException(String message) {
            super(message);
        }
    }
    public static class UsernameAlreadyTakenException extends LoginException {
        public UsernameAlreadyTakenException(String message) {
            super(message);
        }
    }

}
