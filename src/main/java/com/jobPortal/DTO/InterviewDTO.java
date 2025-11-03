package com.jobPortal.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterviewDTO {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Scheduled date is required")
    private Date scheduledDate;

    @NotBlank(message = "Interview mode is required")
    private String mode;

    @NotBlank(message = "Interview status is required")
    private String status;

    @Size(max = 2000, message = "Feedback can contain at most 2000 characters")
    private String feedback;
}
