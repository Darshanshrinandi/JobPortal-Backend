package com.jobPortal.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobDTO {


    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Job category ID is required")
    private Long categoryId;

    private Long jobId;


    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Title can be at most 100 characters")
    @Column(name = "name", nullable = false)
    private String title;

    @Size(max = 5000, message = "Description can be at most 5000 characters")
    private String description;

    @Size(max = 2000, message = "Requirement can be at most 2000 characters")
    private String requirement;

    private String salaryRange;

    @NotBlank(message = "Job type is required")
    private String jobType;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Status is required")
    private String status;

    // Many-to-many relationship
    @NotEmpty(message = "At least one skill is required")
    private Set<@NotBlank String> skillNames;
}
