package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.ApplicationDTO;
import com.jobPortal.Model.Application;
import com.jobPortal.Model.User;
import com.jobPortal.Service.ApplicationService;

import com.jobPortal.Service.UserService;
import io.jsonwebtoken.io.IOException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/jobPortal/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;



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

    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwnerByJobId(#id, authentication)")
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

    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwnerByJobId(#jobId, authentication)")
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<ApiResponse<ApplicationDTO>> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam Long jobId,       // ✅ include this!
            @RequestParam String status) {

        ApplicationDTO updated = applicationService.updateApplicationStatus(id, status);

        ApiResponse<ApplicationDTO> response =
                new ApiResponse<>(HttpStatus.OK.value(), "Application status updated", updated);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwnerByJobId(#id, authentication)")
    @GetMapping("/resume/{id}")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id) throws IOException, MalformedURLException {

        Application application = applicationService.getApplicationEntityById(id);
        if (application == null) {
            throw new RuntimeException("Application not found for id " + id);
        }


        User user = application.getUser();
        if (user == null) {
            throw new RuntimeException("User not found for application " + id);
        }


        Resource resource = userService.getResume(user.getUserId());


        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + Paths.get(user.getResume()).getFileName().toString() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwnerByApplicationId(#id, authentication)")
    @PutMapping("/shortlist/{id}")
    public ResponseEntity<ApiResponse<ApplicationDTO>> shortlistCandidate(
            @PathVariable Long id,
            @RequestParam boolean shortlisted) {
        ApplicationDTO updated = applicationService.shortlistCandidate(id, shortlisted);
        ApiResponse<ApplicationDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Candidate shortlist status updated", updated);
        return ResponseEntity.ok(response);
    }



    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwnerByJobId(#jobId, authentication)")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> searchApplications(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String status) {

        List<ApplicationDTO> result = applicationService.searchApplications(skill, status);
        ApiResponse<List<ApplicationDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Applications filtered successfully", result);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/checkApplied")
    public ResponseEntity<ApiResponse<Boolean>> checkIfApplied(
            @RequestParam Long userId,
            @RequestParam Long jobId) {

        boolean alreadyApplied = applicationService.existsByUserIdAndJobId(userId, jobId);

        ApiResponse<Boolean> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                alreadyApplied ? "User has already applied" : "User has not applied",
                alreadyApplied
        );

        return ResponseEntity.ok(response);
    }




}
