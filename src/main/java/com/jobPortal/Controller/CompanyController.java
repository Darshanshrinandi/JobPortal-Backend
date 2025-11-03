package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.CompanyDTO;
import com.jobPortal.Model.Company;
import com.jobPortal.Service.CompanyService;
import jakarta.annotation.security.PermitAll;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jobPortal/company")
public class CompanyController {


    @Autowired
    private CompanyService companyService;

    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwner(#id,authentication.principal.id)")
    @GetMapping("/findCompany/{id}")
    public ResponseEntity<ApiResponse<Company>> getCompany(@PathVariable Long id) {
        Company company = companyService.getCompanyById(id);

        ApiResponse<Company> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched Successfully",
                company
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Create Company
    @PermitAll
    @PostMapping(value = "/createCompany", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Company>> createCompany(
            @Valid @RequestPart("company") CompanyDTO companyDTO,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile
    ) throws IOException, MessagingException {
        companyDTO.setLogoFile(logoFile);
        Company savedCompany = companyService.createCompany(companyDTO);

        ApiResponse<Company> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Saved Successfully",
                savedCompany
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwner(#id,authentication.principal.id)")
    @PutMapping(value = "/updateCompany/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Company>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestPart("company") CompanyDTO company,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile
    ) throws IOException {

        company.setLogoFile(logoFile);
        Company updatedCompany = companyService.updateCompany(id, company);

        ApiResponse<Company> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Updated Successfully",
                updatedCompany
        );
        return new ResponseEntity<>(response, HttpStatus.OK);


    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping(value = "/getAllCompanies")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {

        Page<CompanyDTO> companies = companyService.getCompanies(page, size, sortBy);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("companies", companies);
        responseData.put("currentPage", companies.getNumber());
        responseData.put("totalItems", companies.getTotalElements());
        responseData.put("totalPages", companies.getTotalPages());

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Companies fetched successfully",
                responseData
        );

        return new ResponseEntity<>(response, HttpStatus.OK);


    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Company>>> searchCompanies(
            @RequestParam String keyword) {

        List<Company> companies = companyService.searchCompany(keyword);

        String message = companies.isEmpty()
                ? "No companies found for keyword: " + keyword
                : "Search result fetched successfully";

        ApiResponse<List<Company>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                message,
                companies
        );

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwner(#id,authentication.principal.id)")
    @PutMapping("/activate/{id}")
    public ResponseEntity<ApiResponse<String>> activateCompany(@PathVariable Long id) {
        companyService.activateCompany(id);

        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Company Activated",
                "company id " + id

        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwner(#id,authentication.id)")
    @PutMapping("deactivate/{id}")
    public ResponseEntity<ApiResponse<String>> deactivateCompany(@PathVariable Long id) {
        companyService.deactivateCompany(id);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Company Deactivated",
                "company id" + id
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/averageRatings/{id}")
    public ResponseEntity<ApiResponse<Double>> getAverageRatings(@PathVariable Long id) {
        double ratings = companyService.getAverageRatings(id);

        ApiResponse<Double> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched Successfully",
                ratings
        );

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ADMIN') or @companyService.isCompanyOwner(#id,authentication.principal.id)")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);

        ApiResponse<Boolean> response = new ApiResponse<>(
                HttpStatus.NO_CONTENT.value(),
                "Company Deleted",
                true
        );

        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
}
