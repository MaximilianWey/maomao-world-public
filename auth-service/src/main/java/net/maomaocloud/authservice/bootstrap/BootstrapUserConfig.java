package net.maomaocloud.authservice.bootstrap;

import java.util.List;

public record BootstrapUserConfig(List<BootstrapUser> admins, List<BootstrapUser> users) {
    public record BootstrapUser(String username,
                                String email,
                                String password,
                                String displayName,
                                String avatarUrl) {
    }
}
