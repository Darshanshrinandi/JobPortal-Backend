package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.InterviewDTO;
import com.jobPortal.Service.InterviewService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobPortal/interview")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@companyService.isCompanyOwnerByApplicationId(#interviewDTO.applicationId, authentication)"
    )
    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<InterviewDTO>> scheduleInterview(
            @Valid @RequestBody InterviewDTO interviewDTO
    ) throws MessagingException {

        InterviewDTO saved =
                interviewService.scheduleInterview(interviewDTO);

        ApiResponse<InterviewDTO> response =
                new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Interview scheduled successfully",
                        saved
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@interviewService.isAuthorized(#id, principal.id)"
    )
    @PutMapping("/updateInterview/{id}")
    public ResponseEntity<ApiResponse<InterviewDTO>> updateInterview(
            @PathVariable Long id,
            @Valid @RequestBody InterviewDTO interviewDTO
    ) {

        InterviewDTO interview =
                interviewService.updateInterview(id, interviewDTO);

        ApiResponse<InterviewDTO> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Interview updated successfully",
                        interview
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@interviewService.isAuthorized(#id, principal.id)"
    )
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInterview(
            @PathVariable Long id
    ) {

        interviewService.deleteInterview(id);

        ApiResponse<Void> response =
                new ApiResponse<>(
                        HttpStatus.NO_CONTENT.value(),
                        "Interview deleted successfully",
                        null
                );

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(response);
    }

    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@interviewService.isAuthorized(#id, principal.id)"
    )
    @GetMapping("/getInterview/{id}")
    public ResponseEntity<ApiResponse<InterviewDTO>> getInterview(
            @PathVariable Long id
    ) {

        InterviewDTO interviewDTO =
                interviewService.getInterviewById(id);

        ApiResponse<InterviewDTO> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Interview found",
                        interviewDTO
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<InterviewDTO>>> getAllInterview() {

        List<InterviewDTO> interviews =
                interviewService.getAllInterviews();

        ApiResponse<List<InterviewDTO>> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Interviews found",
                        interviews
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@companyService.isCompanyOwnerByApplicationId(#id, authentication)"
    )
    @GetMapping("/application/{id}")
    public ResponseEntity<ApiResponse<List<InterviewDTO>>>
    getInterviewsByApplication(
            @PathVariable Long id
    ) {

        List<InterviewDTO> interviews =
                interviewService.getInterviewsByApplication(id);

        ApiResponse<List<InterviewDTO>> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Interviews found by application",
                        interviews
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InterviewDTO>>>
    getInterviewsByStatus(
            @PathVariable String status
    ) {

        List<InterviewDTO> interviews =
                interviewService.getInterviewsByStatus(status);

        ApiResponse<List<InterviewDTO>> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Interviews found by status",
                        interviews
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@companyService.isCompanyOwnerByJobId(#id, authentication)"
    )
    @PostMapping("/feedback/{id}")
    public ResponseEntity<ApiResponse<String>> addFeedback(
            @PathVariable Long id,
            @RequestParam String feedback
    ) {

        interviewService.addFeedback(id, feedback);

        ApiResponse<String> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Feedback submitted successfully",
                        feedback
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@companyService.isCompanyOwner(#companyId, authentication)"
    )
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<InterviewDTO>>> getInterviewsByCompany(
            @PathVariable Long companyId
    ) {

        List<InterviewDTO> interviews =
                interviewService.getInterviewsByCompany(companyId);

        ApiResponse<List<InterviewDTO>> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Company interviews found",
                        interviews
                );

        return ResponseEntity.ok(response);
    }
}