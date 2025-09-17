package net.maomaocloud.authservice.api.admin;

import net.maomaocloud.authservice.api.auth.oidc.OidcProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService service;

    @Autowired
    public AdminController(AdminService service) {
        this.service = service;
    }

    @PostMapping("/provider/register/oidc")
    public ResponseEntity<?> registerOidc(@RequestBody OidcProvider provider) {
        try {
            var registeredProvider = service.registerAuthProvider(provider);
            return ResponseEntity.ok(registeredProvider);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to register OIDC provider: " + e.getMessage());
        }
    }

    @DeleteMapping("/provider/delete/{id}")
    public ResponseEntity<?> unregisterProvider(@PathVariable UUID id) {
        try {
            service.deleteAuthProvider(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Provider not found: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(service.getUsers());
    }
}
