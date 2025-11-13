package com.jobPortal.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobCategoryDTO {

    private Long categoryId;


    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 100, message = "Category name can be at most 100 characters")
    private String name;
}
