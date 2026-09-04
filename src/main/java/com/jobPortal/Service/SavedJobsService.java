package com.jobPortal.Service;

import com.jobPortal.DTO.SavedJobsDTO;
import com.jobPortal.Model.Job;
import com.jobPortal.Model.SavedJobs;
import com.jobPortal.Model.User;
import com.jobPortal.Repository.JobRepository;
import com.jobPortal.Repository.SavedJobRepository;
import com.jobPortal.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SavedJobsService {


    private final  SavedJobRepository savedJobRepository;


    private final UserRepository userRepository;


    private final JobRepository jobRepository;

    public SavedJobsService(SavedJobRepository savedJobRepository, UserRepository userRepository, JobRepository jobRepository) {
        this.savedJobRepository = savedJobRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

   @Transactional
    public SavedJobsDTO saveJob(SavedJobsDTO savedJobsDTO) {
        User user = userRepository.findById(savedJobsDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Job job = jobRepository.findById(savedJobsDTO.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        boolean alreadySaved = savedJobRepository.existsByUserUserIdAndJobJobId(savedJobsDTO.getUserId(), savedJobsDTO.getJobId());

        if (alreadySaved) {
            throw new RuntimeException("Job is already saved by this user!");
        }

        SavedJobs savedJobs = SavedJobs.builder()
                .user(user)
                .job(job)
                .build();

        savedJobs = savedJobRepository.save(savedJobs);
        return mapToDTO(savedJobs);
    }

    public void removeJob(Long userId, Long jobId) {

        SavedJobs savedJobs = savedJobRepository.findByUserUserIdAndJobJobId(userId, jobId)
                .orElseThrow(() -> new RuntimeException("Saved job not found for userId: " + userId + " and jobId: " + jobId));

        savedJobRepository.delete(savedJobs);
    }

    public List<SavedJobsDTO> getSavedJobsByUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        return savedJobRepository.findByUser(user)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public boolean isJobSaved(Long userId, Long jobId) {

        return savedJobRepository.existsByUserUserIdAndJobJobId(userId, jobId);
    }


    private SavedJobsDTO mapToDTO(SavedJobs savedJobs) {
        return SavedJobsDTO.builder()
                .userId(savedJobs.getUser().getUserId())
                .jobId(savedJobs.getJob().getJobId())
                .build();
    }
}
