package net.maomaocloud.authservice.api.auth.common;

public enum AuthProviderCapabilityLevel {
    USER_SOURCE {
        public boolean canCreateUser() {
            return true;
        }
        public boolean canLogin() {
            return true;
        }
        public boolean canLink() {
            return true;
        }
    },
    LOGIN_ONLY {
        public boolean canCreateUser() {
            return false;
        }
        public boolean canLogin() {
            return true;
        }
        public boolean canLink() {
            return true;
        }
    },
    LINK_ONLY {
        public boolean canCreateUser() {
            return false;
        }
        public boolean canLogin() {
            return false;
        }
        public boolean canLink() {
            return true;
        }
    };

    public abstract boolean canCreateUser();
    public abstract boolean canLogin();
    public abstract boolean canLink();

}
