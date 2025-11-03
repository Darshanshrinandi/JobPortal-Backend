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

    @PreAuthorize("hasRole('ADMIN') or @interviewService.isAuthorized(#interviewDTO.applicationId,principal.id)")
    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<InterviewDTO>> createInterview(
            @Valid @RequestBody InterviewDTO interviewDTO
    ) throws MessagingException {

        InterviewDTO newInterview = interviewService.scheduleInterview(interviewDTO);

        ApiResponse<InterviewDTO> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Interview Scheduled successfully",
                newInterview
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or @interviewService.isAuthorized(#id,principal.id)")
    @PutMapping("/updateInterview/{id}")
    public ResponseEntity<ApiResponse<InterviewDTO>> updateInterview(@PathVariable Long id,
                                                                     @Valid @RequestBody InterviewDTO interviewDTO) {
        InterviewDTO interview = interviewService.updateInterview(id, interviewDTO);

        ApiResponse<InterviewDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Updated successfully",
                interview
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or @interviewService.isAuthorized(#id,principal.id)")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInterview(@PathVariable Long id) {
        interviewService.deleteInterview(id);

        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.NO_CONTENT.value(),
                "Interview Deleted",
                null
        );
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN') or @interviewService.isAuthorized(#id,principal.id)")
    @GetMapping("/getInterview/{id}")
    public ResponseEntity<ApiResponse<InterviewDTO>> getInterview(@PathVariable Long id) {
        InterviewDTO interviewDTO = interviewService.getInterviewById(id);
        ApiResponse<InterviewDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Interview Found",
                interviewDTO
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<InterviewDTO>>> getAllInterview() {
        List<InterviewDTO> interviews = interviewService.getAllInterviews();

        ApiResponse<List<InterviewDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Interviews Found",
                interviews
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or @interviewService.isAuthorized(#id,principal.id)")
    @GetMapping("/application/{id}")
    public ResponseEntity<ApiResponse<List<InterviewDTO>>> getInterviewsByApplication(@PathVariable Long id) {

        List<InterviewDTO> interviews = interviewService.getInterviewsByApplication(id);
        ApiResponse<List<InterviewDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Interviews found by applications",
                interviews
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InterviewDTO>>> getInterviewsByStatus(@PathVariable String status) {
        List<InterviewDTO> interviews = interviewService.getInterviewsByStatus(status);

        ApiResponse<List<InterviewDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Interviews found by status",
                interviews
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
