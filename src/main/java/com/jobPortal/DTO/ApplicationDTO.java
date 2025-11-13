package com.jobPortal.DTO;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationDTO {

    private Long applicationId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Job ID is required")
    private Long jobId;

    private MultipartFile resumeFile;


    private boolean shortlisted = false;

    @Size(max = 2000, message = "Cover letter can contain at most 2000 characters")
    private String coverLetter;

    @NotBlank(message = "Status is required")
    private String status;


    private String userName;

    private String email;

    @Temporal(TemporalType.TIMESTAMP)
    private Date appliedDate = new Date();

}
