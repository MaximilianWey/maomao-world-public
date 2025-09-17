package net.maomaocloud.authservice.api.users;

public record UpdateUserDTO(String username,
                            String email,
                            String displayName,
                            String avatarUrl) {

    public UpdateUserDTO {
        if (username != null) {
            username = username.toLowerCase();
        }
        if (email != null) {
            email = email.toLowerCase();
        }
    }
}
