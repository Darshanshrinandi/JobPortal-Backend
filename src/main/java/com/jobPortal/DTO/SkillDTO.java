package com.jobPortal.DTO;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillDTO {
    @NotBlank(message = "Skill name cannot be blank")
    private String name;

    private boolean active = true;
}
