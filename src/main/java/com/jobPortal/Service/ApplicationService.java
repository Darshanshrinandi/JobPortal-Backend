package com.jobPortal.Service;

import com.jobPortal.DTO.ApplicationDTO;
import com.jobPortal.Model.Application;
import com.jobPortal.Model.Company;
import com.jobPortal.Model.Job;
import com.jobPortal.Model.User;
import com.jobPortal.Repository.ApplicationRepository;
import com.jobPortal.Repository.JobRepository;
import com.jobPortal.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationService {


    private final ApplicationRepository applicationRepository;


    private final UserRepository userRepository;


    private final JobRepository jobRepository;

    private final NotificationService notificationService;

    @Value("${uploads.dir}")
    private  String uploadDir;


    private final EmailService emailService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              UserRepository userRepository,
                              JobRepository jobRepository, NotificationService notificationService,

                              EmailService emailService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;

        this.emailService = emailService;
    }

    private final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final String[] ALLOWED_FILE_TYPES = {".pdf", ".doc", ".docx"};

    public ApplicationDTO createApplication(ApplicationDTO applicationDTO) {

        User user = userRepository.findById(applicationDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found by this id " + applicationDTO.getUserId()));

        Job job = jobRepository.findById(applicationDTO.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found by this id " + applicationDTO.getJobId()));

        if (applicationRepository.existsByUserUserIdAndJobJobId(user.getUserId(), job.getJobId())) {
            throw new RuntimeException("You already applied for this job");
        }

        Application application = Application.builder()
                .user(user)
                .job(job)
                .coverLetter(applicationDTO.getCoverLetter())
                .status(applicationDTO.getStatus())
                .appliedDate(new Date())
                .build();
        if (applicationDTO.getResumeFile() != null && !applicationDTO.getResumeFile().isEmpty()) {
            String resumePath = uploadFile(applicationDTO.getResumeFile());
            application.setResume(resumePath);
        }
        application = applicationRepository.save(application);
        return mapToDTO(application);
    }

    public ApplicationDTO updateApplication(Long id, ApplicationDTO applicationDTO) throws MessagingException {

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found by this id " + id));

        Company company = application.getJob().getCompany();

        if (applicationDTO.getCoverLetter() != null) {
            application.setCoverLetter(applicationDTO.getCoverLetter());
        }
        if (applicationDTO.getStatus() != null) {
            String oldStatus = application.getStatus();
            String newStatus = applicationDTO.getStatus();

            application.setStatus(newStatus);

            if (!newStatus.equalsIgnoreCase(oldStatus)) {
                User user = application.getUser();
                Job job = application.getJob();

                switch (newStatus.toUpperCase()) {
                    case "SUBMITTED" -> emailService.sendApplicationStatusEmail(
                            user.getEmail(),
                            job.getTitle(),

                            "Your application has been submitted successfully",
                            company
                    );
                    case "SHORTLISTED" -> emailService.sendApplicationStatusEmail(
                            user.getEmail(),
                            job.getTitle(),

                            "Congratulation you have been shortlisted for " + job.getTitle() + " .",
                            company
                    );
                    case "REJECTED" -> emailService.sendApplicationStatusEmail(
                            user.getEmail(),
                            job.getTitle(),
                            "We regrated to inform you that your application not selected",
                            company
                    );
                    case "SELECTED" -> emailService.sendApplicationStatusEmail(
                            user.getEmail(),
                            job.getTitle(),
                            "Great news you have been selected for " + job.getTitle() + "!",
                            company
                    );

                }

            }


        }

        if (applicationDTO.getResumeFile() != null && !applicationDTO.getResumeFile().isEmpty()) {
            String resumePath = uploadFile(applicationDTO.getResumeFile());
            application.setResume(resumePath);
        }
        application = applicationRepository.save(application);
        return mapToDTO(application);
    }

    public void deleteApplication(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found by this id " + id));

        if (application.getResume() != null) {
            Path filePath = Paths.get(uploadDir).resolve(application.getResume());

            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete Resume " + e.getMessage());
            }

        }

        applicationRepository.delete(application);
    }
    @Transactional
    public ApplicationDTO shortlistCandidate(Long id, boolean shortlisted) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setShortlisted(shortlisted);
        applicationRepository.save(application);

        return mapToDTO(application);
    }




    public ApplicationDTO getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found by this id " + id));
        return mapToDTO(application);
    }

    public List<ApplicationDTO> getAllApplications() {

        List<ApplicationDTO> applications = applicationRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
        return applications;

    }

    public List<ApplicationDTO> getApplicationByUser(Long id) {

        return applicationRepository.findByUserUserId(id).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());


    }

    public boolean existsByUserIdAndJobId(Long userId, Long jobId) {
        return applicationRepository.existsByUser_UserIdAndJob_JobId(userId, jobId);
    }


    public List<ApplicationDTO> getApplicationByJob(Long id) {

        return applicationRepository.findByJobJobId(id).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    public Application getApplicationEntityById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found by id " + id));
    }


    private String uploadFile(MultipartFile file) {
        try {

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("File is too large");
            }
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            boolean allowed = false;

            for (String ext : ALLOWED_FILE_TYPES) {
                if (originalFileName.toLowerCase().endsWith(ext)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new RuntimeException("Invalid file type,Only PDF,DOC,DOCX allowed");
            }

            Path appliedResumesDir = Paths.get(uploadDir, "applied-resumes");
            if (!Files.exists(appliedResumesDir)) {
                Files.createDirectories(appliedResumesDir);
            }

            String fileName = System.currentTimeMillis() + "_" + originalFileName;
            Path targetLocation = appliedResumesDir.resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation);

            return "applied-resumes/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store the resume " + e.getMessage());
        }
    }

    public ApplicationDTO updateApplicationStatus(Long id, String status)
            throws MessagingException {

        Application application = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Application not found by id: " + id
                        ));

        String oldStatus = application.getStatus();

        application.setStatus(status);

        Application saved = applicationRepository.save(application);

        User user = saved.getUser();
        Job job = saved.getJob();
        Company company = job.getCompany();

        if (oldStatus == null || !status.equalsIgnoreCase(oldStatus)) {

            String title;
            String message;

            switch (status.toUpperCase()) {

                case "SHORTLISTED" -> {
                    title = "Application Shortlisted";
                    message =
                            company.getName()
                                    + " has shortlisted your application for "
                                    + job.getTitle()
                                    + ".";
                }

                case "REJECTED" -> {
                    title = "Application Update";
                    message =
                            company.getName()
                                    + " has rejected your application for "
                                    + job.getTitle()
                                    + ".";
                }

                case "SELECTED" -> {
                    title = "Congratulations!";
                    message =
                            company.getName()
                                    + " has selected you for "
                                    + job.getTitle()
                                    + ".";
                }

                case "SUBMITTED" -> {
                    title = "Application Submitted";
                    message =
                            "Your application for "
                                    + job.getTitle()
                                    + " at "
                                    + company.getName()
                                    + " has been submitted successfully.";
                }

                default -> {
                    title = "Application Status Updated";
                    message =
                            company.getName()
                                    + " updated your application status for "
                                    + job.getTitle()
                                    + " to "
                                    + status
                                    + ".";
                }
            }

            notificationService.createNotification(
                    user,
                    saved,
                    "APPLICATION_STATUS",
                    title,
                    message
            );
        }

        return mapToDTO(saved);
    }


    private ApplicationDTO mapToDTO(Application application) {
        return ApplicationDTO.builder()
                .applicationId(application.getApplicationId())
                .userId(application.getUser().getUserId())
                .jobId(application.getJob().getJobId())
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .resumeFile(null)
                .appliedDate(application.getAppliedDate() != null
                        ? application.getAppliedDate()
                        : null)
                .userName(application.getUser() != null ? application.getUser().getName() : null)
                .email(application.getUser() != null ? application.getUser().getEmail() : null)
                .build();
    }

    public boolean isApplicationOwner(Long applicationId, Long userId) {
        return applicationRepository.findById(applicationId)
                .map(application -> application.getUser().getUserId().equals(userId))
                .orElse(false);
    }

    public List<ApplicationDTO> searchApplications(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return applicationRepository.searchApplications(keyword.trim())
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
}
