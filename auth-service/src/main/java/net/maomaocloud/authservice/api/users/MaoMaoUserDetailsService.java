package net.maomaocloud.authservice.api.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class MaoMaoUserDetailsService implements UserDetailsService {

    private final UserService service;

    @Autowired
    public MaoMaoUserDetailsService(UserService service) {
        this.service = service;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = service.findUser(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        return new MaoMaoUserDetails(user);
    }
}
