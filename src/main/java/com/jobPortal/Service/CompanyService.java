package com.jobPortal.Service;

import com.jobPortal.Jwt.JwtService;
import com.jobPortal.DTO.CompanyDTO;
import com.jobPortal.Model.Company;
import com.jobPortal.Model.Review;
import com.jobPortal.Repository.CompanyRepository;
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

@Service
@Transactional
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private JwtService jwtService;

    @Value("${uploads.dir}")
    private String UPLOAD_DIR;

    public String verifyCompany(String email, String password) {
        Company company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (passwordEncoder.matches(password, company.getPassword())) {
            return jwtService.generateToken(email); // can use a separate token type if needed
        } else {
            throw new RuntimeException("Authentication Failed");
        }
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

        if (!company.getEmail().equals(companyDTO.getEmail()) &&
                companyRepository.existsByEmail(companyDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        company.setName(companyDTO.getName());
        company.setEmail(companyDTO.getEmail());
        company.setPassword(passwordEncoder.encode(companyDTO.getPassword()));
        company.setDescription(companyDTO.getDescription());
        company.setWebSite(companyDTO.getWebSite());
        company.setLocation(companyDTO.getLocation());
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

    public boolean isCompanyOwner(Long companyId, Long authentication) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found by this id " + companyId));

        return company.getCompanyId().equals(authentication);
    }
}
