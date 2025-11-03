package com.jobPortal.DTO;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDTO {

    @NotBlank(message = "Role name cannot be blank")
    private String name;
}
