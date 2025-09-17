package net.maomaocloud.authservice.bootstrap;

import net.maomaocloud.authservice.api.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class ClearUsers {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClearUsers.class);

    private final UserService userService;

    @Value("${bootstrap.users.clear:false}")
    private boolean clearUsers;

    public ClearUsers(UserService userService) {
        this.userService = userService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(10)
    public void onApplicationReady() {
        if (!clearUsers) {
            LOGGER.info("User clearing is disabled");
            return;
        }

        LOGGER.warn("Clearing all users and profiles...");

        userService.deleteAll();

        LOGGER.info("All users and user profiles have been deleted.");
    }
}
