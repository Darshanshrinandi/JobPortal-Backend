package com.jobPortal.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private Long userId;


    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 20, message = "Name should be between 2 and 20 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;


    @Size(min = 4, message = "Password should contain at least 4 characters")
    private String password;

    @NotBlank(message = "Experience cannot be blank")
    private String experience;

    private Set<String> roleNames;
    private Set<String> skillNames;

    private String resume;
    private String profilePicture;

    private Set<String> preferredLocations;
    private Set<String> preferredCategories;
}
