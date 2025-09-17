package net.maomaocloud.authservice.api.users;

import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final LinkedAccountRepository linkedAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${DEFAULT_PROFILE_PICTURE_URL:https://i.pinimg.com/736x/56/0d/8d/560d8dcb636bb844508e4da0c6626c39.jpg}")
    private String defaultProfilePictureUrl;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserProfileRepository profileRepository,
                       LinkedAccountRepository linkedAccountRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.linkedAccountRepository = linkedAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public boolean userExists(String identifier) {
        return userRepository.findByEmailOrUsername(identifier).isPresent();
    }

    public Optional<User> findUser(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUser(String identifier) {
        return userRepository.findByEmailOrUsername(identifier);
    }

    public Optional<UserProfile> findUserProfile(UUID id) {
        return profileRepository.findById(id);
    }

    public Optional<UserProfile> findUserProfile(String identifier) {
        return findUser(identifier)
                .map(User::getId)
                .flatMap(profileRepository::findById);
    }

    public void deleteAll() {
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Transactional
    public Optional<User> createUser(AuthProvider provider,
                                     String username,
                                     String email,
                                     String rawPassword,
                                     String avatarUrl,
                                     String displayName) {
        if (userExists(username, email, provider)) {
            return Optional.empty();
        }

        User newUser = buildNewUser(username,
                email,
                rawPassword,
                provider.getMetadata().getId()
        );

        switch (provider.getMetadata().getType()) {
            case OIDC -> {
                if (rawPassword != null) {
                    throw new IllegalArgumentException("Password provided for OIDC user" + username);
                }
            }
            case LDAP -> {
                if (rawPassword == null) {
                    throw new IllegalArgumentException("No password provided for LDAP user: " + username);
                }

            }
            case LOCAL -> {
                if (rawPassword == null) {
                    throw new IllegalArgumentException("No password provided for LOCAL user: " + username);
                }
            }
        }
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            LOGGER.info("No avatar URL provided for user '{}', using default profile picture.", username);
            return saveAndLogUser(newUser, provider, displayName, defaultProfilePictureUrl);
        }
        LOGGER.info("Creating user '{}' with avatar URL '{}'", username, avatarUrl);
        return saveAndLogUser(newUser, provider, displayName, avatarUrl);
    }

    private Optional<User> saveAndLogUser(User newUser, AuthProvider provider, String displayName, String avatarUrl) {
        try {
            User savedUser = userRepository.save(newUser);
            LOGGER.info("{} User '{}' has been successfully created.", provider.getMetadata().getType(), savedUser.getUsername());

            UserProfile profile = new UserProfile(savedUser, displayName, avatarUrl);
            profileRepository.save(profile);

            String logMessage = displayName != null
                    ? String.format("UserProfile for '%s' created with display name '%s'", savedUser.getUsername(), displayName)
                    : String.format("UserProfile for '%s' created", savedUser.getUsername());
            LOGGER.info(logMessage);

            return Optional.of(savedUser);
        } catch (Exception e) {
            LOGGER.error("Failed to create user '{}': {}", newUser.getUsername(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    private boolean userExists(String username, String email, AuthProvider provider) {
        if (userRepository.findByUsername(username).isPresent()) {
            LOGGER.warn("User '{}' already exists. Provider: {}", username, provider.getMetadata().getType());
            return true;
        }

        if (userRepository.findByEmail(email).isPresent()) {
            LOGGER.warn("User with email '{}' already exists. Provider: {}", email, provider.getMetadata().getType());
            return true;
        }

        return false;
    }

    private User buildNewUser(String username, String email, String rawPassword, UUID providerId) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        if (rawPassword != null) {
            newUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        newUser.setProviderId(providerId);

        return newUser;
    }

    public User linkAccount(UserProfile userProfile, LinkedAccount linkedAccount) {
        if (userProfile.getLinkedAccounts().contains(linkedAccount)) {
            LOGGER.warn("User '{}' already has linked account '{}'",
                    userProfile.getUser().getUsername(),
                    linkedAccount.getProviderId()
            );
        } else {
            userProfile.addLinkedAccount(linkedAccount);
            this.profileRepository.save(userProfile);
        }
        return findUser(userProfile.getUserId())
                .orElseThrow();
    }

    public UserProfile updateUserProfile(UUID userId, UpdateUserDTO updateUserDTO) {
        User user = findUser(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        UserProfile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + user.getUsername()));

        if (updateUserDTO.username() != null) {
            user.setUsername(updateUserDTO.username());
        }
        if (updateUserDTO.email() != null) {
            user.setEmail(updateUserDTO.email());
        }

        if (updateUserDTO.displayName() != null) {
            profile.setDisplayName(updateUserDTO.displayName());
        }
        if (updateUserDTO.avatarUrl() != null) {
            profile.setAvatarUrl(updateUserDTO.avatarUrl());
        }
        profile.setUser(user);
        return profileRepository.save(profile);
    }

    public Optional<LinkedAccount> findAccountLinkByExternalIdAndProviderId(String subject, UUID id) {
        return linkedAccountRepository.findByExternalIdAndProviderId(subject, id);
    }

    public Optional<LinkedAccount> findById(UUID id) {
        return linkedAccountRepository.findById(id);
    }

    public void disconnectProvider(UUID userId, UUID linkId) {
        Optional<LinkedAccount> linkedAccount = findById(linkId);
        if (linkedAccount.isPresent()) {
            linkedAccountRepository.delete(linkedAccount.get());
            LOGGER.info("Successfully disconnected user {} from provider {}",
                    linkedAccount.get().getUserProfile().getUser().getUsername(),
                    linkedAccount.get().getProviderName());
        } else {
            LOGGER.warn("No linked account found for user {} and link id {}", userId, linkId);
        }
    }
}
