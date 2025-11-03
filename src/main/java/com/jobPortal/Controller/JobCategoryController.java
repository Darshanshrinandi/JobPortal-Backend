package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.JobCategoryDTO;
import com.jobPortal.Model.JobCategory;
import com.jobPortal.Service.JobCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/jobPortal/job-categories")
public class JobCategoryController {

    @Autowired
    private JobCategoryService jobCategoryService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/findCategory/{id}")
    public ResponseEntity<ApiResponse<Optional<JobCategory>>> findCategory(@PathVariable Long id) {

        Optional<JobCategory> category = jobCategoryService.getCategoryById(id);

        ApiResponse<Optional<JobCategory>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Data fetched successfully",
                category
        );

        return new ResponseEntity<>(response, HttpStatus.OK);


    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createCategory")
    public ResponseEntity<ApiResponse<JobCategoryDTO>> createCategory(@Valid @RequestBody JobCategoryDTO jobCategory) {
        JobCategoryDTO category = jobCategoryService.createJobCategory(jobCategory);

        ApiResponse<JobCategoryDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "category created successfully",
                category
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateCategory/{id}")
    public ResponseEntity<ApiResponse<JobCategoryDTO>> updateJCategory(@PathVariable Long id, @Valid @RequestBody JobCategoryDTO jobCategoryDTO) {

        JobCategoryDTO categoryDTO = jobCategoryService.updateJobCategory(id, jobCategoryDTO);

        ApiResponse<JobCategoryDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "updated Successfully",
                categoryDTO
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJCategory(@PathVariable Long id) {

        jobCategoryService.deleteJobCategory(id);

        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "deleted successfully",
                null
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
