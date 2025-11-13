package com.jobPortal.Service;

import com.jobPortal.Jwt.JwtService;
import com.jobPortal.DTO.CompanyDTO;
import com.jobPortal.Model.Company;
import com.jobPortal.Model.Job;
import com.jobPortal.Model.Review;
import com.jobPortal.Repository.ApplicationRepository;
import com.jobPortal.Repository.CompanyRepository;
import com.jobPortal.Repository.JobRepository;
import com.jobPortal.Security.CompanyDetailsImpl;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Service("companyService")
@Transactional
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationRepository applicationRepository;


    @Autowired
    private JwtService jwtService;

    @Value("${uploads.dir}")
    private String UPLOAD_DIR;

    public String verifyCompany(String email, String password) {
        Company company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Company not found with email: " + email));

        if (!passwordEncoder.matches(password, company.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }


        return jwtService.generateToken(company.getCompanyId(), company.getEmail(), "COMPANY");
    }

    public List<Company> getAllCompanies(){
        return companyRepository.findAll();
    }


    public Company createCompany(CompanyDTO companyDTO) throws IOException, MessagingException {
        if (companyRepository.existsByEmail(companyDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Company company = new Company();
        company.setName(companyDTO.getName());
        company.setEmail(companyDTO.getEmail());
        company.setPassword(passwordEncoder.encode(companyDTO.getPassword()));
        company.setDescription(companyDTO.getDescription());
        company.setWebSite(companyDTO.getWebSite());
        company.setLocation(companyDTO.getLocation());
        company.setCreatedAt(new Date());

        MultipartFile logo = companyDTO.getLogoFile();
        if (logo != null && !logo.isEmpty()) {
            validateLogoFile(logo);
            company.setLogo(saveLogoFile(logo));
        }
        Company savedCompany = companyRepository.save(company);
        emailService.sendCompanyWelcomeEmail(savedCompany.getEmail(), savedCompany.getName(), savedCompany);
        return savedCompany;
    }


    public Company updateCompany(Long id, CompanyDTO companyDTO) throws IOException {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));


        if (companyDTO.getEmail() != null && !company.getEmail().equals(companyDTO.getEmail())) {
            if (companyRepository.existsByEmail(companyDTO.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            company.setEmail(companyDTO.getEmail());
        }


        if (companyDTO.getName() != null) company.setName(companyDTO.getName());
        if (companyDTO.getDescription() != null) company.setDescription(companyDTO.getDescription());
        if (companyDTO.getWebSite() != null) company.setWebSite(companyDTO.getWebSite());
        if (companyDTO.getLocation() != null) company.setLocation(companyDTO.getLocation());


        if (companyDTO.getPassword() != null && !companyDTO.getPassword().isBlank()) {
            company.setPassword(passwordEncoder.encode(companyDTO.getPassword()));
        }

        company.setUpdatedAt(new Date());


        MultipartFile logoFile = companyDTO.getLogoFile();
        if (logoFile != null && !logoFile.isEmpty()) {
            validateLogoFile(logoFile);
            deleteOldLogo(company.getLogo());
            company.setLogo(saveLogoFile(logoFile));
        }

        return companyRepository.save(company);
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found by this id " + id));
    }


    public Page<CompanyDTO> getCompanies(int page, int size, String sortBy) {
        Page<Company> companies = companyRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy).ascending()));

        return companies.map(company -> CompanyDTO.builder()
                .companyId(company.getCompanyId())
                .name(company.getName())
                .email(company.getEmail())
                .description(company.getDescription())
                .webSite(company.getWebSite())
                .location(company.getLocation())
                .status(company.getStatus())
                .build()
        );
    }


    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found by this id " + id));

        deleteOldLogo(company.getLogo());
        companyRepository.delete(company);
    }

    public List<Company> searchCompany(String keyword) {
        List<Company> companies = companyRepository.searchByKeyword(keyword);
        if (companies.isEmpty()) {
            return List.of();
        }
        return companies;
    }


    public void activateCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found by this id " + id));
        company.setStatus("ACTIVE");
        companyRepository.save(company);
    }

    public void deactivateCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found by this id " + id));
        company.setStatus("INACTIVE");
        companyRepository.save(company);
    }


    public double getAverageRatings(Long id) {
        Company company = getCompanyById(id);
        List<Review> reviews = company.getReviews();

        if (reviews.isEmpty())
            return 0;

        return reviews.stream().mapToDouble(Review::getRatings).average().orElse(0);
    }


    private String saveLogoFile(MultipartFile multipartFile) throws IOException {

        Path logoDir = Paths.get(UPLOAD_DIR, "logo").toAbsolutePath().normalize();
        Files.createDirectories(logoDir);


        String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(multipartFile.getOriginalFilename());

        Path filePath = logoDir.resolve(fileName);
        multipartFile.transferTo(filePath.toFile());


        return "uploads/logo/" + fileName;
    }

    private void deleteOldLogo(String logoPath) {
        if (logoPath != null) {
            File file = new File(logoPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void validateLogoFile(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        if (contentType == null || (!contentType.equals("image/png")
                && !contentType.equals("image/jpeg")
                && !contentType.equals("image/jpg"))) {
            throw new RuntimeException("Only PNG and JPEG files are allowed");
        }
        if (multipartFile.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("Logo file should not exceed 2MB");
        }
    }

    public boolean isCompanyOwner(Long companyId, Authentication authentication) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found by this id " + companyId));

        // Assuming your principal has a getId() method returning companyId
        Long principalId = ((CompanyDetailsImpl) authentication.getPrincipal()).getId();

        return company.getCompanyId().equals(principalId);
    }

    public boolean isCompanyOwnerByJobId(Long jobId, Authentication authentication){

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found by this id " + jobId));

         Company company = job.getCompany();
         if(company ==null){
             throw new RuntimeException("Company not found by this id " + jobId);
         }

         Long companyId = company.getCompanyId();
         Long principalId = ((CompanyDetailsImpl) authentication.getPrincipal()).getId();
         return companyId.equals(principalId);
    }
    public boolean isCompanyOwnerByApplicationId(Long applicationId, Authentication authentication) {
        String email = authentication.getName();
        return applicationRepository.findById(applicationId)
                .map(app -> app.getJob().getCompany().getEmail().equals(email))
                .orElse(false);
    }

}
