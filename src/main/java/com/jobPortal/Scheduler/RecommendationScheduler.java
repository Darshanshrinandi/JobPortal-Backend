package com.jobPortal.Scheduler;


import com.jobPortal.Model.Job;
import com.jobPortal.Model.User;
import com.jobPortal.Repository.CompanyRepository;
import com.jobPortal.Repository.JobRepository;
import com.jobPortal.Repository.UserRepository;
import com.jobPortal.Service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationScheduler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CompanyRepository companyRepository;

    // Scheduled every Monday at 9 AM
    @Async
    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendWeeklyRecommendedJobs() throws MessagingException {

        List<User> users = userRepository.findAll();
        List<Job> jobs = jobRepository.findAll();


        for(User user : users){

            List<String> recommendedJobs = jobs.stream()
                    .filter(job -> user.getSkills().stream()
                            .anyMatch(skill -> job.getTitle().toLowerCase().contains(skill.getName().toLowerCase()))
                            || user.getPreferredLocations().contains(job.getLocation())
                            ||user.getPreferredCategories().contains(job.getCategory())
                    )
                    .map(job -> job.getTitle()+" at "+job.getCompany().getName()+" in "+job.getLocation())
                    .collect(Collectors.toList());

            if(!recommendedJobs.isEmpty()){

                for(Job job: jobs){
                    if(recommendedJobs.contains(job.getTitle()+" at "+job.getCompany().getName()+" in "+job.getLocation())){
                        emailService.sendRecommendedJobsEmail(
                                user.getEmail(),
                                List.of(job.getTitle()+" at "+job.getCompany().getName()+" in "+job.getLocation()),
                                job.getCompany()
                        );
                    }
                }

            }
        }
    }

}
