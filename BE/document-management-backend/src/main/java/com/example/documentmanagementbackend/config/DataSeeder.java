package com.example.documentmanagementbackend.config;

import com.example.documentmanagementbackend.entity.Permission;
import com.example.documentmanagementbackend.entity.Role;
import com.example.documentmanagementbackend.entity.User;
import com.example.documentmanagementbackend.repository.PermissionRepository;
import com.example.documentmanagementbackend.repository.RoleRepository;
import com.example.documentmanagementbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Permission read = upsertPermission("DOCUMENT_READ");
        Permission write = upsertPermission("DOCUMENT_WRITE");
        Permission delete = upsertPermission("DOCUMENT_DELETE");
        Permission userMgmt = upsertPermission("USER_MANAGEMENT");

        Role adminRole = upsertRole("ROLE_ADMIN", Set.of(read, write, delete, userMgmt));
        upsertRole("ROLE_MODERATOR", Set.of(read, write));

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setEmail("admin@gov.local");
            admin.setFullName("System Administrator");
            admin.setIsActive(true);
            admin.setRoles(new HashSet<>(List.of(adminRole)));
            userRepository.save(admin);
        }
    }

    private Permission upsertPermission(String name) {
        return permissionRepository.findByName(name).orElseGet(() -> {
            Permission permission = new Permission();
            permission.setName(name);
            return permissionRepository.save(permission);
        });
    }

    private Role upsertRole(String roleName, Set<Permission> permissions) {
        Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName);
            return newRole;
        });
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }
}
