package kz.yermek.util;

import kz.yermek.models.Role;
import kz.yermek.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!roleRepository.existsByName("ROLE_USER")) {
            Role role = new Role(null, "Role for users", "ROLE_USER");
            roleRepository.save(role);
        }
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            Role role = new Role(null, "Role for admins", "ROLE_ADMIN");
            roleRepository.save(role);
        }
    }
}
