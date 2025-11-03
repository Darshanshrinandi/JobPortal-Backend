package com.jobPortal.Service;

import com.jobPortal.DTO.InterviewDTO;
import com.jobPortal.Model.*;
import com.jobPortal.Repository.ApplicationRepository;
import com.jobPortal.Repository.InterviewRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private EmailService emailService;

    public InterviewDTO scheduleInterview(InterviewDTO interviewDTO) throws MessagingException {

        Application application = applicationRepository.findById(interviewDTO.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application Not Found"));

        Interview interview = Interview.builder()
                .application(application)
                .scheduledDate(interviewDTO.getScheduledDate())
                .mode(interviewDTO.getMode())
                .status(interviewDTO.getStatus())
                .feedback(interviewDTO.getFeedback())
                .build();

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
                job.getCompany().getName(),
                job.getLocation(),
                company

        );


        return mapToDTO(interview);

    }

    public InterviewDTO updateInterview(Long id, InterviewDTO interviewDTO) {

        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview Not Found"));

        if (interviewDTO.getScheduledDate() != null) {
            interview.setScheduledDate(interviewDTO.getScheduledDate());
        }

        if (interviewDTO.getMode() != null) {
            interview.setMode(interviewDTO.getMode());
        }
        if (interviewDTO.getStatus() != null) {
            interview.setStatus(interviewDTO.getStatus());
        }
        if (interviewDTO.getFeedback() != null) {
            interview.setFeedback(interviewDTO.getFeedback());
        }
        interview = interviewRepository.save(interview);
        return mapToDTO(interview);
    }

    public void deleteInterview(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview Not Found"));
        interviewRepository.deleteById(id);
    }

    public InterviewDTO getInterviewById(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview Not Found"));
        return mapToDTO(interview);
    }

    public List<InterviewDTO> getAllInterviews() {
        return interviewRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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

        return InterviewDTO.builder()
                .applicationId(interview.getApplication().getApplicationId())
                .scheduledDate(interview.getScheduledDate())
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
}
