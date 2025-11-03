package com.jobPortal.Controller;

import com.jobPortal.DTO.*;
import com.jobPortal.Jwt.JwtService;
import com.jobPortal.Model.Company;
import com.jobPortal.Model.Skill;
import com.jobPortal.Model.User;
import com.jobPortal.Service.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/jobPortal/public")
public class PublicController {


    @Autowired
    private UserService userService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SkillService skillService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JobService jobService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JobCategoryService jobCategoryService;

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return new ResponseEntity<>("Public test endpoint working!", HttpStatus.OK);
    }

    @PostMapping("/companyLogin")
    public ResponseEntity<LoginResponse> loginAsCompany(@RequestBody CompanyLoginDTO loginRequest, HttpServletResponse response) {
        String token = companyService.verifyCompany(loginRequest.getEmail(), loginRequest.getPassword());

        Cookie cookie = new Cookie("jwtToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtService.getJwtExpirationMs()/1000));

        response.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponse(token));
    }


    @PostMapping("/userLogin")
    public ResponseEntity<LoginResponse> loginAsUser(@RequestBody UserLoginDTO loginRequest,HttpServletResponse response) {
        String token = userService.verifyUser(loginRequest.getEmail(), loginRequest.getPassword());

        Cookie cookie = new Cookie("jwtToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtService.getJwtExpirationMs()/1000));
        response.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponse(token));
    }


    @PostMapping(value = "/addUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<User>> createUser(
            @Valid @RequestPart("user") UserDTO userDTO,
            @RequestPart(value = "resume", required = false) MultipartFile resumeFile,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture
    ) throws IOException, MessagingException {

        User createdUser = userService.createUser(userDTO, resumeFile, profilePicture);

        ApiResponse<User> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "User created successfully",
                createdUser
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/user/logout")
    public ResponseEntity<String> logoutAsUser(HttpServletResponse response) {
         Cookie cookie = new Cookie("jwtToken",null);
         cookie.setHttpOnly(true);
         cookie.setSecure(false);
         cookie.setPath("/");
         cookie.setMaxAge(0);
         response.addCookie(cookie);

         return new ResponseEntity<>(" logged out successfully",HttpStatus.OK);
    }

    @PostMapping("/company/logout")
    public ResponseEntity<String> logoutAsCompany(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwtToken",null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return new ResponseEntity<>(" logged out successfully",HttpStatus.OK);
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobDTO>>> getAllJobs() {

        List<JobDTO> jobs = jobService.getAllJobs();

        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                jobs
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/getSkill/{id}")
    public ResponseEntity<ApiResponse<Skill>> getSkill(@PathVariable Long id) {
        Skill skill = skillService.findSkillById(id);

        ApiResponse<Skill> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Skill retrieved successfully",
                skill
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getAllSkills() {
        List<Skill> allSkills = skillService.getAllSkills();

        ApiResponse<List<Skill>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched All Skills",
                allSkills
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewDTO>> getReviewById(@PathVariable Long id) {
        ReviewDTO review = reviewService.getReviewById(id);
        ApiResponse<ReviewDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Review Details",
                review
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/findJob/{id}")
    public ResponseEntity<ApiResponse<JobDTO>> findJobById(@PathVariable Long id) {
        JobDTO jobDTO = jobService.getJobById(id);

        ApiResponse<JobDTO> response = new ApiResponse<>(HttpStatus.OK.value(), "Job found", jobDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/findJobs/company/{id}")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByCompanyId(@PathVariable Long id) {
        List<JobDTO> jobs = jobService.getJobsByCompanyId(id);

        ApiResponse<List<JobDTO>> response = new ApiResponse<>(HttpStatus.OK.value(), "Jobs found", jobs);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping("/findJobs/category/{id}")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByCategoryId(@PathVariable Long id) {
        List<JobDTO> jobs = jobService.getJobsByCategoryId(id);

        ApiResponse<List<JobDTO>> response = new ApiResponse<>(HttpStatus.OK.value(), "Jobs found", jobs);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/findJobs/status")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByStatus(@RequestParam String status) {
        List<JobDTO> jobs = jobService.getJobsByStatus(status);
        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Jobs found", jobs
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/findJobs/location")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByLocation(@RequestParam String location) {
        List<JobDTO> jobs = jobService.getJobsByLocation(location);
        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Jobs found", jobs);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/findJobs/jobType")
    public ResponseEntity<ApiResponse<List<JobDTO>>> findJobByJobType(@RequestParam String jobType) {
        List<JobDTO> jobs = jobService.getJobsByJobType(jobType);

        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Jobs found", jobs
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

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

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<JobCategoryDTO>>> findAllJCategory() {

        List<JobCategoryDTO> categoryDTOS = jobCategoryService.getAllCategory();

        ApiResponse<List<JobCategoryDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All Job Categories",
                categoryDTOS
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<JobDTO>>> search(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String location) {

        List<JobDTO> jobs = jobService.search(key, location);

        ApiResponse<List<JobDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Jobs found", jobs
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


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

}
