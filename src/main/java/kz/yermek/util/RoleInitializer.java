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
    private final RoleRepository rolesRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (!rolesRepository.existsByName("ROLE_USER")) {

            Role roleUser = new Role(null, "This role is for users.", "ROLE_USER");
            rolesRepository.save(roleUser);
        }
        if (!rolesRepository.existsByName("ROLE_ADMIN")) {

            Role roleUser = new Role(null, "This role is for admins.", "ROLE_ADMIN");
            rolesRepository.save(roleUser);
        }
    }
}
