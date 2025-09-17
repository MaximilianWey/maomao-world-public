package net.maomaocloud.authservice.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import net.maomaocloud.authservice.api.auth.AuthProviderRegistry;
import net.maomaocloud.authservice.api.auth.AuthenticationService;
import net.maomaocloud.authservice.api.auth.common.AuthProvider;
import net.maomaocloud.authservice.api.auth.common.AuthProviderService;
import net.maomaocloud.authservice.api.auth.common.AuthProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class AuthProviderInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthProviderInitializer.class);

    @Value("classpath:config/bootstrap-providers.json")
    private Resource bootstrapProviders;

    private final List<AuthProviderService> services;
    private final AuthenticationService authenticationService;
    private final AuthProviderRegistry registry;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthProviderInitializer(List<AuthProviderService> services,
                                   AuthProviderRegistry registry,
                                   ObjectMapper objectMapper,
                                   AuthenticationService authenticationService) {
        this.services = services;
        this.registry = registry;
        this.objectMapper = objectMapper;
        this.authenticationService = authenticationService;
    }

    @PostConstruct
    public void init() {
        try {
            registerBootstrapProviders();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize AuthProviderInitializer", e);
        }
        registerAllProvidersInMemory();
    }

    public void registerBootstrapProviders() throws IOException {
        LOGGER.info("Registering all bootstrap auth providers");
        List<AuthProvider> authProviders = objectMapper.readValue(bootstrapProviders.getInputStream(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, AuthProvider.class));

        authProviders.stream()
                .filter(provider -> authenticationService.getProviderMetadataByName(provider.getMetadata().getProviderName()).isEmpty())
                .peek(provider -> LOGGER.info("Registering new provider: {}", provider.getMetadata().getProviderName()))
                .forEach(provider -> {
                    services.stream()
                            .filter(services -> !services.managingType().equals(AuthProviderType.LOCAL))
                            .filter(service
                                    -> service.managingType().equals(provider.getMetadata().getType())
                            ).findFirst()
                            .ifPresent(services -> {
                                var savedProvider = services.registerProvider(provider);
                                LOGGER.info("Registered provider: {} with ID: {}",
                                        savedProvider.getMetadata().getProviderName(),
                                        savedProvider.getMetadata().getId());
                            });
                });

    }

    public void registerAllProvidersInMemory() {
        LOGGER.info("Registering all auth providers in memory");
        for (AuthProviderService service : services) {
            LOGGER.debug("Registering providers from service: {}", service.getClass().getSimpleName());
            service.getProviders().forEach(registry::registerProvider);
        }
    }
}
