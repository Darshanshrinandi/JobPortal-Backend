package com.jobPortal.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ApplicationDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Job ID is required")
    private Long jobId;

    private MultipartFile resumeFile;

    @Size(max = 2000, message = "Cover letter can contain at most 2000 characters")
    private String coverLetter;

    @NotBlank(message = "Status is required")
    private String status;
}
