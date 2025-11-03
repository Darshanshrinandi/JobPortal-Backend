package com.jobPortal.Service;

import com.jobPortal.Model.Company;
import com.jobPortal.Model.Job;
import com.jobPortal.Model.User;
import com.jobPortal.Repository.JobRepository;
import com.jobPortal.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender jobPortalMailSender;


    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String jobPortalMail;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;


    @Async
    public void sendUserWelcomeEmail(String toEmail, String username, String password) throws MessagingException {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("password", password);
        String body = templateEngine.process("user-welcome.html", context);

        sendMail(jobPortalMailSender, jobPortalMail, toEmail, "Welcome to JobPortal", body);
    }

    @Async
    public void sendCompanyWelcomeEmail(String toEmail, String companyName, Company company) throws MessagingException {

        JavaMailSender companySender = createCompanyMail(company.getEmail(), company.getPassword());
        Context context = new Context();
        context.setVariable("companyName", companyName);
        context.setVariable("toEmail", toEmail);

        String body = templateEngine.process("company-welcome.html", context);

        sendMail(jobPortalMailSender, jobPortalMail, toEmail, "Welcome to jobportal as a company", body);

    }

    @Async
    public void sendNewJobNotification(String toEmail, String jobTitle, String companyName, String location, String salary, Company company)
            throws MessagingException {
        JavaMailSender companySender = createCompanyMail(company.getEmail(), company.getPassword());
        Context context = new Context();
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("companyName", companyName);
        context.setVariable("location", location);
        context.setVariable("salary", salary);

        String body = templateEngine.process("new-job-notification.html", context);

        sendMail(jobPortalMailSender, jobPortalMail, toEmail, "New Job Posted: " + jobTitle, body);
    }

    @Async
    public void sendApplicationStatusEmail(String toEmail, String jobTitle, String status, Company company) throws MessagingException {
        JavaMailSender companySender = createCompanyMail(company.getEmail(), company.getPassword());
        Context context = new Context();
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("status", status);

        String body = templateEngine.process("application-status.html", context);

        sendMail(jobPortalMailSender, jobPortalMail, toEmail, "Application status updated", body);
    }

    @Async
    public void sendInterviewScheduleEmail(String toEmail,
                                           String username,
                                           String jobTitle,
                                           String interviewDate,
                                           String mode,
                                           String companyName,
                                           String location,
                                           Company company) throws MessagingException {

        // Prepare the Thymeleaf context
        JavaMailSender mailSender = createCompanyMail(company.getEmail(), company.getPassword());
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("interviewDate", interviewDate);
        context.setVariable("mode", mode);
        context.setVariable("companyName", companyName);
        context.setVariable("location", location);

        // Generate HTML email body
        String body = templateEngine.process("interview-schedule.html", context);

        // Send the email
        sendMail(jobPortalMailSender, jobPortalMail, toEmail, "Your Interview is Scheduled!", body);
    }

    @Async
    @Scheduled(cron = "0 0 8 ? * SUN") // Runs every Sunday at 8 AM
    public void sendWeeklyJobDigest() throws MessagingException {
        List<User> users = userRepository.findAll();
        LocalDate lastWeek = LocalDate.now().minusDays(7);
        List<Job> recentJobs = jobRepository.findJobsPostedLastWeek(lastWeek);

        for (User user : users) {
            // Filter jobs for the user based on skills, locations, or categories
            List<Map<String, Object>> personalizedJobsForEmail = recentJobs.stream()
                    .filter(job -> user.getSkills().stream()
                            .anyMatch(skill -> job.getTitle().toLowerCase().contains(skill.getName().toLowerCase()))
                            || user.getPreferredLocations().contains(job.getLocation())
                            || user.getPreferredCategories().contains(job.getCategory().getName())
                    )
                    .map(job -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("title", job.getTitle());
                        map.put("companyName", job.getCompany().getName());
                        map.put("location", job.getLocation());
                        map.put("salary", job.getSalaryRange());
                        map.put("postedDate", job.getPostedDate());
                        map.put("applyLink", "http://localhost:8085/jobs/" + job.getJobId()); // Apply link
                        return map;
                    })
                    .collect(Collectors.toList());

            if (personalizedJobsForEmail.isEmpty()) continue;

            // Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("username", user.getName());
            context.setVariable("jobs", personalizedJobsForEmail);

            // Generate HTML email
            String body = templateEngine.process("weekly-job-digest.html", context);

            // Send email
            sendMail(jobPortalMailSender, jobPortalMail, user.getEmail(), "Weekly Job Digest", body);
        }
    }


    public void sendRecommendedJobsEmail(String toEmail, List<String> recommendedJobs, Company company) throws MessagingException {
        JavaMailSender companySender = createCompanyMail(company.getEmail(), company.getPassword());
        Context context = new Context();
        context.setVariable("jobs", recommendedJobs);
        String body = templateEngine.process("recommended-jobs.html", context);

        sendMail(jobPortalMailSender, jobPortalMail, toEmail, "Recommended jobs for you", body);

    }

    @Async
    public void sendCompanyAnnouncement(String toEmail, String announcements, Company company) throws MessagingException {
        JavaMailSender companySender = createCompanyMail(company.getEmail(), company.getPassword());
        Context context = new Context();
        context.setVariable("announcements", announcements);
        String body = templateEngine.process("company-announcement.html", context);

        sendMail(companySender, company.getEmail(), toEmail, "company announcement", body);
    }

    @Async
    public void sendAdminAlert(String toEmail, String message) throws MessagingException {
        Context context = new Context();
        context.setVariable("message", message);
        String body = templateEngine.process("admin-alert.html", context);
        sendMail(jobPortalMailSender, jobPortalMail, toEmail, "System Alerts", body);
    }

    @Async
    public void sendCareerTipsEmail(String toEmail, String tip) throws MessagingException {
        Context context = new Context();
        context.setVariable("tip", tip);
        String body = templateEngine.process("career-tips.html", context);
        sendMail(jobPortalMailSender, jobPortalMail, toEmail, "Career tips & newsLetter", body);
    }

    @Async
    public void sendSubscriptionConfirmation(String toEmail, String subscriptionType) throws MessagingException {
        Context context = new Context();
        context.setVariable("subscriptionType", subscriptionType);
        String body = templateEngine.process("subscription-confirmation.html", context);
        sendMail(jobPortalMailSender, jobPortalMail, toEmail, "Subscription confirmed", body);
    }


    public JavaMailSender createCompanyMail(String email, String password) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setPort(587);
        javaMailSender.setUsername(email);
        javaMailSender.setPassword(password);

        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }


    private void sendMail(JavaMailSender mailSender, String from, String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}

