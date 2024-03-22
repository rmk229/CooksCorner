package kz.yermek.services.impl;

import kz.yermek.models.Role;
import kz.yermek.repositories.RoleRepository;
import kz.yermek.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    @Override
    public Optional<Role> getUserRole() {
        return roleRepository.findByName("ROLE_USER");
    }
}
