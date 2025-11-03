package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.SavedJobsDTO;
import com.jobPortal.Service.SavedJobsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobPortal/saved-jobs")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class SavedJobsController {
    @Autowired
    private SavedJobsService savedJobsService;

    @PreAuthorize("hasRole('ADMIN') or #savedJobsDTO.userId == authentication.principal.id")
    @PostMapping("/saveJob")
    public ResponseEntity<ApiResponse<SavedJobsDTO>> saveJob(@Valid @RequestBody SavedJobsDTO savedJobsDTO) {

        SavedJobsDTO savedJob = savedJobsService.saveJob(savedJobsDTO);

        ApiResponse<SavedJobsDTO> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Job is saved successfully",
                savedJob
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @DeleteMapping("/removeJob/{userId}/{jobId}")
    public ResponseEntity<ApiResponse<Void>> removeJob(@PathVariable Long userId,
                                                       @PathVariable Long jobId) {

        savedJobsService.removeJob(userId, jobId);

        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Job is deleted successfully",
                null
        );

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<List<SavedJobsDTO>>> getSavedJobsByUser(@PathVariable Long id) {
        List<SavedJobsDTO> savedJobs = savedJobsService.getSavedJobsByUser(id);

        ApiResponse<List<SavedJobsDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Jobs saved successfully",
                savedJobs
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> isJobSaved(@RequestParam Long userId,
                                                           @RequestParam Long jobId) {
        boolean isSaved = savedJobsService.isJobSaved(userId, jobId);
        String message = isSaved ? "Job is already saved" : "Job is not saved";
        ApiResponse<Boolean> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                message,
                isSaved
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
