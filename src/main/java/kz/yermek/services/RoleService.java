package kz.yermek.services;

import kz.yermek.models.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> getUserRole();
}
