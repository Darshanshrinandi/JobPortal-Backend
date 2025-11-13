package com.jobPortal.Service;

import com.jobPortal.DTO.JobDTO;
import com.jobPortal.Model.*;
import com.jobPortal.Repository.*;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service("jobService")
@Transactional
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobCategoryRepository jobCategoryRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    public JobDTO createJob(JobDTO jobDTO, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with id " + companyId));

        JobCategory jobCategory = jobCategoryRepository.findById(jobDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Job category not found with id " + jobDTO.getCategoryId()));

        Job job = buildJobFromDTO(jobDTO, company, jobCategory);
        job = jobRepository.save(job);

        String companyName = (job.getCompany() != null && job.getCompany().getName() != null)
                ? job.getCompany().getName() : "Unknown company";

        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            try {
                emailService.sendNewJobNotification(
                        user.getEmail(),
                        job.getTitle(),
                        companyName,
                        job.getLocation(),
                        job.getSalaryRange() != null ? job.getSalaryRange().toString() : "Not disclosed",
                        company
                );
            } catch (MessagingException e) {
                System.out.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
            }
        }

        return mapToDTO(job);
    }

    public JobDTO updateJob(Long id, JobDTO jobDTO, Long companyId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found by id " + id));

        if (!job.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Unauthorized: Cannot edit another company's job");
        }

        if (jobDTO.getTitle() != null) job.setTitle(jobDTO.getTitle());
        if (jobDTO.getDescription() != null) job.setDescription(jobDTO.getDescription());
        if (jobDTO.getRequirement() != null) job.setRequirement(jobDTO.getRequirement());
        if (jobDTO.getSalaryRange() != null) job.setSalaryRange(jobDTO.getSalaryRange());
        if (jobDTO.getJobType() != null) job.setJobType(jobDTO.getJobType());
        if (jobDTO.getLocation() != null) job.setLocation(jobDTO.getLocation());
        if (jobDTO.getStatus() != null) job.setStatus(jobDTO.getStatus());

        if (jobDTO.getCompanyId() != null) {
            Company company = companyRepository.findById(jobDTO.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Company not found with id " + jobDTO.getCompanyId()));
            job.setCompany(company);
        }

        if (jobDTO.getCategoryId() != null) {
            JobCategory jobCategory = jobCategoryRepository.findById(jobDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id " + jobDTO.getCategoryId()));
            job.setCategory(jobCategory);
        }

        if (jobDTO.getSkillNames() != null) {
            Set<Skill> skills = jobDTO.getSkillNames().stream()
                    .map(name -> skillRepository.findByName(name)
                            .orElseGet(() -> skillRepository.save(new Skill(null, name))))
                    .collect(Collectors.toSet());
            job.setSkills(skills);
        }

        job = jobRepository.save(job);
        return mapToDTO(job);
    }

    public void deleteJob(Long id, Long companyId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found by id " + id));
        if (!job.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Unauthorized: Cannot delete another company's job");
        }
        jobRepository.delete(job);
    }

    public JobDTO getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found by id " + id));
        return mapToDTO(job);
    }

    public List<JobDTO> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<JobDTO> getJobsByCompanyId(Long id) {
        return jobRepository.findByCompanyCompanyId(id).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<JobDTO> getJobsByCategoryId(Long id) {
        return jobRepository.findByCategoryCategoryId(id).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<JobDTO> getJobsByStatus(String status) {
        return jobRepository.findByStatusIgnoreCase(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<JobDTO> getJobsByLocation(String location) {
        return jobRepository.findByLocationIgnoreCaseContaining(location).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<JobDTO> getJobsByJobType(String jobType) {
        return jobRepository.findByJobTypeIgnoreCaseContaining(jobType).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<JobDTO> getJobsByDateRanges(LocalDate startDate, LocalDate endDate) {
        return jobRepository.findByPostedDateBetween(startDate, endDate).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<JobDTO> search(String key, String location) {
        return jobRepository.searchByTitleDescriptionSkillsOrLocation(key, location).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    private Job buildJobFromDTO(JobDTO jobDTO, Company company, JobCategory jobCategory) {
        Job job = Job.builder()
                .company(company)
                .category(jobCategory)
                .title(jobDTO.getTitle())
                .description(jobDTO.getDescription())
                .requirement(jobDTO.getRequirement())
                .salaryRange(jobDTO.getSalaryRange())
                .jobType(jobDTO.getJobType())
                .location(jobDTO.getLocation())
                .status(jobDTO.getStatus())
                .postedDate(LocalDate.now())
                .build();

        if (jobDTO.getSkillNames() != null) {
            Set<Skill> skills = jobDTO.getSkillNames().stream()
                    .filter(name -> name != null && !name.isBlank())
                    .map(name -> skillRepository.findByName(name)
                            .orElseGet(() -> skillRepository.save(Skill.builder().name(name).build())))
                    .collect(Collectors.toSet());
            job.setSkills(skills);
        }


        return job;
    }

    public Map<String, Object> getJobStats(Long companyId) {
        List<Job> jobs = jobRepository.findByCompanyCompanyId(companyId);
        long total = jobs.size();
        long active = jobs.stream().filter(j -> "ACTIVE".equalsIgnoreCase(j.getStatus())).count();
        long closed = jobs.stream().filter(j -> "CLOSED".equalsIgnoreCase(j.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs", total);
        stats.put("activeJobs", active);
        stats.put("closedJobs", closed);
        return stats;
    }


    private JobDTO mapToDTO(Job job) {
        Set<String> skillNames = job.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        return JobDTO.builder()
                .companyId(job.getCompany().getCompanyId())
                .categoryId(job.getCategory().getCategoryId())
                .jobId(job.getJobId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirement(job.getRequirement())
                .salaryRange(job.getSalaryRange())
                .jobType(job.getJobType())
                .location(job.getLocation())
                .status(job.getStatus())
                .skillNames(skillNames)
                .build();
    }




    public boolean isJobOwner(Long jobId, Long companyId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found by id " + jobId));
        return job.getCompany().getCompanyId().equals(companyId);
    }

}
