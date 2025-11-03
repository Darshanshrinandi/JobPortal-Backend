package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.ApplicationDTO;
import com.jobPortal.Service.ApplicationService;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/jobPortal/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;



    @PreAuthorize("hasRole('USER')")
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<ApplicationDTO>> createApplication(
            @Valid @RequestPart("application") ApplicationDTO applicationDTO,
            @RequestPart(value = "resumeFile",required = false)MultipartFile file
            ){

        if(file!= null){
            applicationDTO.setResumeFile(file);
        }

        ApplicationDTO createApplication = applicationService.createApplication(applicationDTO);
        ApiResponse<ApplicationDTO> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Application submitted Successfully",
                createApplication
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("@applicationService.isApplicationOwner(#id,authentication.principal.id)")
    @PutMapping("/updateApplication/{id}")
    public ResponseEntity<ApiResponse<ApplicationDTO>> updateApplication(@PathVariable Long id,
                                                                         @Valid @RequestPart("application") ApplicationDTO applicationDTO,
                                                                         @RequestPart(value = "resumeFile",required = false)MultipartFile file
                                                                         ) throws MessagingException {
        if(file!= null){
            applicationDTO.setResumeFile(file);
        }
        ApplicationDTO updateApplication = applicationService.updateApplication(id, applicationDTO);

        ApiResponse<ApplicationDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Application updated successfully",
                updateApplication
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("@applicationService.isApplicationOwner(#id,authentication.principal.id)")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(@PathVariable Long id){
        applicationService.deleteApplication(id);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.NO_CONTENT.value(),
                "Application deleted successfully",
                null
        );
        return  new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getAllApplications(){

        List<ApplicationDTO> applications = applicationService.getAllApplications();
        ApiResponse<List<ApplicationDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All application fecthed successfully",
                applications
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or @applicationService.isApplicationOwner(#id,authentication.principal.id)")
    @GetMapping("/getApplication/{id}")
    public ResponseEntity<ApiResponse<ApplicationDTO>> getApplication(@PathVariable Long id){
        ApplicationDTO applicationDTO = applicationService.getApplicationById(id);

        ApiResponse<ApplicationDTO> response= new ApiResponse<>(
                HttpStatus.OK.value(),
                "Application fetched  successfully",
                applicationDTO
        );

        return new ResponseEntity<>(response,HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ADMIN') or @applicationService.isApplicationOwner(#id,authentication.principal.id)")
    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getApplicationsByUser(@PathVariable Long id){
          List<ApplicationDTO> applications = applicationService.getApplicationByUser(id);

          ApiResponse<List<ApplicationDTO>> response = new ApiResponse<>(
                  HttpStatus.OK.value(),
                  "Applications by user fetched successfully",
                  applications
          );
          return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/job/{id}")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getApplicationsByJob(@PathVariable Long id){

        List<ApplicationDTO> applications = applicationService.getApplicationByJob(id);

        ApiResponse<List<ApplicationDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Applications by job fetched successfully",
                applications
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
