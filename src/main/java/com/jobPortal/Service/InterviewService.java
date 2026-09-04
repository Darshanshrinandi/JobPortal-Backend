package com.jobPortal.Service;

import com.jobPortal.DTO.InterviewDTO;
import com.jobPortal.Model.*;
import com.jobPortal.Repository.ApplicationRepository;
import com.jobPortal.Repository.InterviewRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewService {


    private final InterviewRepository interviewRepository;


    private final ApplicationRepository applicationRepository;


    private final EmailService emailService;

    private final NotificationService notificationService;

    public InterviewService(InterviewRepository interviewRepository, ApplicationRepository applicationRepository, EmailService emailService, NotificationService notificationService) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Transactional
    public InterviewDTO scheduleInterview(
            InterviewDTO interviewDTO
    ) throws MessagingException {

        Application application = applicationRepository
                .findById(interviewDTO.getApplicationId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Application Not Found"
                        ));

        Interview interview = Interview.builder()
                .application(application)
                .scheduledDate(interviewDTO.getScheduledDate())
                .mode(interviewDTO.getMode())
                .status(interviewDTO.getStatus())
                .feedback(interviewDTO.getFeedback())
                .createdAt(new Date())
                .build();


        System.out.println("========== INTERVIEW DEBUG ==========");
        System.out.println("createdAt: " + interview.getCreatedAt());
        System.out.println("scheduledDate: " + interview.getScheduledDate());
        System.out.println("applicationId: " + interview.getApplication().getApplicationId());
        System.out.println("====================================");

        interview = interviewRepository.save(interview);

        User user = application.getUser();
        Job job = application.getJob();
        Company company = job.getCompany();


        emailService.sendInterviewScheduleEmail(
                user.getEmail(),
                user.getName(),
                job.getTitle(),
                interview.getScheduledDate().toString(),
                interview.getMode(),
                company.getName(),
                job.getLocation(),
                company
        );

        // New in-app notification
        notificationService.createNotification(
                user,
                application,
                "INTERVIEW_SCHEDULED",
                "Interview Scheduled",
                company.getName()
                        + " has scheduled an interview for "
                        + job.getTitle()
                        + " on "
                        + interview.getScheduledDate()
                        + "."
        );

        return mapToDTO(interview);
    }

    public List<InterviewDTO> getInterviewsByApplication(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application Not Found"));
        return interviewRepository.findByApplicationApplicationId(application.getApplicationId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    public List<InterviewDTO> getInterviewsByStatus(String status) {
        return interviewRepository.findByStatusIgnoreCase(status)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public InterviewDTO mapToDTO(Interview interview) {

        Application application = interview.getApplication();

        User user = application.getUser();

        Job job = application.getJob();

        return InterviewDTO.builder()
                .interviewId(interview.getInterviewId())
                .applicationId(application.getApplicationId())

                .candidateName(
                        user != null ? user.getName() : "N/A"
                )

                .candidateEmail(
                        user != null ? user.getEmail() : "N/A"
                )

                .jobTitle(
                        job != null ? job.getTitle() : "N/A"
                )

                .scheduledDate(interview.getScheduledDate())
                .createdAt(interview.getCreatedAt())
                .mode(interview.getMode())
                .status(interview.getStatus())
                .feedback(interview.getFeedback())
                .build();
    }

    public boolean isAuthorized(Long interviewId, Long companyId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview Not Found"));
        Job job = interview.getApplication().getJob();

        Company ownerCompany = job.getCompany();

        return ownerCompany.getCompanyId().equals(companyId);
    }

    public void addFeedback(Long interviewId, String feedback) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found by id " + interviewId));
        interview.setFeedback(feedback);
        interviewRepository.save(interview);
    }

    public InterviewDTO updateInterview(
            Long id,
            InterviewDTO interviewDTO
    ) {

        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Interview Not Found"));

        Application application = interview.getApplication();

        boolean dateChanged =
                interviewDTO.getScheduledDate() != null &&
                        !interviewDTO.getScheduledDate()
                                .equals(interview.getScheduledDate());

        boolean modeChanged =
                interviewDTO.getMode() != null &&
                        !interviewDTO.getMode()
                                .equalsIgnoreCase(interview.getMode());

        boolean statusChanged =
                interviewDTO.getStatus() != null &&
                        !interviewDTO.getStatus()
                                .equalsIgnoreCase(interview.getStatus());

        if (interviewDTO.getScheduledDate() != null) {
            interview.setScheduledDate(
                    interviewDTO.getScheduledDate()
            );
        }

        if (interviewDTO.getMode() != null) {
            interview.setMode(
                    interviewDTO.getMode()
            );
        }

        if (interviewDTO.getStatus() != null) {
            interview.setStatus(
                    interviewDTO.getStatus()
            );
        }

        if (interviewDTO.getFeedback() != null) {
            interview.setFeedback(
                    interviewDTO.getFeedback()
            );
        }

        interview = interviewRepository.save(interview);

        User user = application.getUser();
        Job job = application.getJob();
        Company company = job.getCompany();

        if (dateChanged || modeChanged) {

            notificationService.createNotification(
                    user,
                    application,
                    "INTERVIEW_RESCHEDULED",
                    "Interview Updated",
                    company.getName()
                            + " updated your interview for "
                            + job.getTitle()
                            + ". New date: "
                            + interview.getScheduledDate()
                            + ", Mode: "
                            + interview.getMode()
                            + "."
            );
        }

        if (statusChanged &&
                "CANCELLED".equalsIgnoreCase(
                        interview.getStatus()
                )) {

            notificationService.createNotification(
                    user,
                    application,
                    "INTERVIEW_CANCELLED",
                    "Interview Cancelled",
                    company.getName()
                            + " cancelled your interview for "
                            + job.getTitle()
                            + "."
            );
        }

        return mapToDTO(interview);
    }

    public InterviewDTO getInterviewById(Long id) {
        return interviewRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() ->
                        new RuntimeException("Interview not found with id: " + id)
                );
    }

    public List<InterviewDTO> getAllInterviews() {
        return interviewRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public void deleteInterview(Long id) {
        if (!interviewRepository.existsById(id)) {
            throw new RuntimeException("Interview not found with id: " + id);
        }

        interviewRepository.deleteById(id);
    }

    public List<InterviewDTO> getInterviewsByCompany(Long companyId) {

        return interviewRepository
                .findByApplicationJobCompanyCompanyId(companyId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }



}
