package net.maomaocloud.authservice.api.users;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "maomao_auth")
public class User {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Username is mandatory")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]{3,16}$",
            message = "Username can only contain english letters, numbers and \"_\" (underscore) and must be between 3 and 16 symbols long."
    )
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "last_login_at")
    private Timestamp lastLoginAt;

    @Column(name = "provider_metadata_id", nullable = false)
    private UUID providerId;

    // Constructors
    public User() {}

    public User(String username, String passwordHash, String email, UUID providerId) {
        this.username = username != null ? username.toLowerCase() : null;
        this.passwordHash = passwordHash;
        this.email = email;
        this.providerId = providerId;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.toLowerCase() : null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Timestamp lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public UUID getProvider() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }
}
