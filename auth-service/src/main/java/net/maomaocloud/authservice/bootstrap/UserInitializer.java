package net.maomaocloud.authservice.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.maomaocloud.authservice.api.auth.local.LocalAuthService;
import net.maomaocloud.authservice.api.users.User;
import net.maomaocloud.authservice.api.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInitializer.class);

    private final UserService userService;
    private final LocalAuthService localAuthService;
    private final ObjectMapper objectMapper;

    @Value("classpath:config/bootstrap-users.json")
    private Resource bootstrapUsers;

    @Value("${bootstrap.users.enabled:true}")
    private boolean bootstrapUsersEnabled;

    @Autowired
    public UserInitializer(UserService userService, LocalAuthService localAuthService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.localAuthService = localAuthService;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(20)
    public void onApplicationReady() {
        if (bootstrapUsersEnabled) {
            LOGGER.info("Bootstrapping users is enabled");
            try {
                bootstrapUsers();
            } catch (Exception e) {
                LOGGER.error("Failed to bootstrap users", e);
            }
        } else {
            LOGGER.info("Bootstrapping users is not enabled");
        }
    }

    private void bootstrapUsers() throws Exception {
        BootstrapUserConfig config = objectMapper.readValue(bootstrapUsers.getInputStream(), BootstrapUserConfig.class);

        for (BootstrapUserConfig.BootstrapUser admin : config.admins()) {
            if (userService.userExists(admin.username())) {
                LOGGER.info("Admin {} already exists, skipping...", admin.username());
            } else {
                Optional<User> savedUser = userService.createUser(localAuthService.getAuthProvider(),
                        admin.username(),
                        admin.email(),
                        admin.password(),
                        admin.avatarUrl(),
                        admin.displayName()
                );
                savedUser.ifPresent(u -> LOGGER.info("Successfully created admin '{}'", u.getUsername()));
            }
        }

        for (BootstrapUserConfig.BootstrapUser user : config.users()) {
            if (userService.userExists(user.username())) {
                LOGGER.info("User '{}' already exists, skipping...", user.username());
            } else {
                Optional<User> savedUser = userService.createUser(localAuthService.getAuthProvider(),
                        user.username(),
                        user.email(),
                        user.password(),
                        user.avatarUrl(),
                        user.displayName()
                );
                savedUser.ifPresent(u -> LOGGER.info("Successfully created user {}", u.getUsername()));
            }
        }
    }
}
