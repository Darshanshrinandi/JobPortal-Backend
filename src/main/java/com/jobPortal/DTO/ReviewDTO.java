package com.jobPortal.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ReviewDTO {

    private Long reviewId;
    private Date createdAt;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @Min(value = 1, message = "Ratings must be at least 1")
    @Max(value = 5, message = "Ratings cannot exceed 5")
    private int ratings;

    @Size(max = 1000, message = "Comment can be at most 1000 characters")
    private String comment;
}
