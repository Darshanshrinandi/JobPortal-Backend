package com.jobPortal.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyDTO {

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name can contain at most 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password should have at least 4 characters")
    private String password;

    @Size(max = 1000, message = "Description can consist max 1000 characters")
    private String description;

    @Size(max = 100, message = "Website link can consist max 100 characters")
    private String webSite;

    @Size(max = 200, message = "Location can be at most 200 characters")
    private String location;

    private String status;
    ;


    private MultipartFile logoFile;
}
