package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.JobDTO;
import com.jobPortal.Service.JobService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jobPortal/job")

public class JobController {

    @Autowired
    private JobService jobService;

//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    @GetMapping("/getAllJobs")
//    public ResponseEntity<ApiResponse<List<JobDTO>>> getAllJobs() {
//
//        List<JobDTO> jobs = jobService.getAllJobs();
//
//        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                jobs
//        );
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwner(#companyId, authentication)")
    @PostMapping("/addJob/{companyId}")
    public ResponseEntity<ApiResponse<JobDTO>> addJob(
            @Valid @RequestBody JobDTO jobDTO,
            @PathVariable Long companyId
    ) {
        JobDTO createdJob = jobService.createJob(jobDTO, companyId);
        ApiResponse<JobDTO> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Job created Successfully",
                createdJob
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PreAuthorize("hasRole('ADMIN') or #companyId == principal.id")
    @PutMapping("/updateJob/{id}/{companyId}")
    public ResponseEntity<ApiResponse<JobDTO>> updateJob(@PathVariable Long id, @PathVariable Long companyId,
                                                         @Valid @RequestBody JobDTO jobDTO) {
        JobDTO updatedJob = jobService.updateJob(id, jobDTO, companyId);

        ApiResponse<JobDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Job updated Successfully",
                updatedJob
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or #companyId == principal.id")
    @DeleteMapping("/deleteJob/{id}/{companyId}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id, @PathVariable Long companyId) {
        jobService.deleteJob(id, companyId);

        return new ResponseEntity<>(new ApiResponse<>(HttpStatus.OK.value(), "Job deleted", null), HttpStatus.OK);
    }

    @PermitAll
    @GetMapping("/findJob/{id}")
    public ResponseEntity<ApiResponse<JobDTO>> findJobById(@PathVariable Long id) {
        JobDTO jobDTO = jobService.getJobById(id);

        ApiResponse<JobDTO> response = new ApiResponse<>(HttpStatus.OK.value(), "Job found", jobDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PreAuthorize("hasAnyRole('USER','ADMIN') or @companyService.isCompanyOwner(#id, authentication)")
    @GetMapping("/findJobs/company/{id}")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByCompanyId(@PathVariable Long id) {
        List<JobDTO> jobs = jobService.getJobsByCompanyId(id);
        ApiResponse<List<JobDTO>> response =
                new ApiResponse<>(HttpStatus.OK.value(), "Jobs found", jobs);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PermitAll
    @GetMapping("/findJobs/category/{id}")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByCategoryId(@PathVariable Long id) {
        List<JobDTO> jobs = jobService.getJobsByCategoryId(id);

        ApiResponse<List<JobDTO>> response = new ApiResponse<>(HttpStatus.OK.value(), "Jobs found", jobs);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PermitAll
    @GetMapping("/findJobs/status")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByStatus(@RequestParam String status) {
        List<JobDTO> jobs = jobService.getJobsByStatus(status);
        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Jobs found", jobs
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PermitAll
    @GetMapping("/findJobs/location")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByLocation(@RequestParam String location) {
        List<JobDTO> jobs = jobService.getJobsByLocation(location);
        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Jobs found", jobs);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PermitAll
    @GetMapping("/findJobs/jobType")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByJobType(@RequestParam String jobType) {
        List<JobDTO> jobs = jobService.getJobsByJobType(jobType);

        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Jobs found", jobs
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PermitAll
    @GetMapping("/findJobs/dateRange")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<JobDTO> jobs = jobService.getJobsByDateRanges(startDate, endDate);

        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Jobs found", jobs
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwner(#companyId, authentication)")
    @GetMapping("/company/{companyId}/jobStats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getJobStats(@PathVariable Long companyId) {
        Map<String, Object> stats = jobService.getJobStats(companyId);
        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Job statistics fetched successfully", stats);
        return ResponseEntity.ok(response);
    }

}
