package com.jobPortal.Service;

import com.jobPortal.DTO.RoleDTO;
import com.jobPortal.Model.Role;
import com.jobPortal.Repository.RoleRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public Role createRole(RoleDTO roleDTO) {
        if (roleDTO.getName() == null || roleDTO.getName().isEmpty()) {
            throw new RuntimeException("Role name cannot be empty");
        }

        return roleRepository.findByName(roleDTO.getName())
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(roleDTO.getName())
                                .build()
                ));
    }

    @Transactional
    public Role updateRole(Long id, RoleDTO roleDTO) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found by this id " + id));

        if (roleDTO.getName() == null || roleDTO.getName().isEmpty()) {
            throw new RuntimeException("Role name cannot be empty");
        }

        existingRole.setName(roleDTO.getName());
        return roleRepository.save(existingRole);
    }

    public Role getRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found by this name " + roleName));
    }

    @Transactional
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found by this id " + id));

        if (!role.getUsers().isEmpty()) {
            throw new RuntimeException("Cannot delete role. It is assigned to users.");
        }

        roleRepository.delete(role);
    }
}
