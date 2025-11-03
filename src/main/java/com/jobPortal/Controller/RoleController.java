package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.RoleDTO;
import com.jobPortal.Model.Role;
import com.jobPortal.Service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/JobPortal/role")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/allRoles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();

        ApiResponse<List<Role>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Roles fetched successfully",
                roles
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getRole/{roleName}")
    public ResponseEntity<ApiResponse<Role>> getRole(@PathVariable String roleName) {
        Role role = roleService.getRoleByName(roleName);

        ApiResponse<Role> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Role found",
                role
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/addRole")
    public ResponseEntity<ApiResponse<Role>> createRole(@RequestBody RoleDTO roleDTO) {
        Role newRole = roleService.createRole(roleDTO);

        ApiResponse<Role> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Role created successfully",
                newRole
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/updateRole/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(@PathVariable Long id,
                                                        @RequestBody RoleDTO roleDTO) {
        Role updatedRole = roleService.updateRole(id, roleDTO);

        ApiResponse<Role> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Role updated successfully",
                updatedRole
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/deleteRole/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
