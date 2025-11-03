package com.jobPortal.Service;

import com.jobPortal.DTO.JobCategoryDTO;
import com.jobPortal.Model.JobCategory;
import com.jobPortal.Repository.JobCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobCategoryService {


    @Autowired
    private JobCategoryRepository jobCategoryRepository;

    public JobCategoryDTO createJobCategory(JobCategoryDTO jobCategoryDTO) {
        if (jobCategoryRepository.existsByNameIgnoreCase(jobCategoryDTO.getName())) {
            throw new RuntimeException("JobCategory already exists " + jobCategoryDTO.getName());
        }
        JobCategory jobCategory = JobCategory.builder()
                .name(jobCategoryDTO.getName())
                .build();
        JobCategory saved = jobCategoryRepository.save(jobCategory);

        return mapToDTO(saved);
    }

    public JobCategoryDTO updateJobCategory(Long id, JobCategoryDTO jobCategoryDTO) {

        JobCategory jobCategory = jobCategoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not find by this id " + id));

        jobCategory.setName(jobCategoryDTO.getName());
        JobCategory updated = jobCategoryRepository.save(jobCategory);
        return mapToDTO(updated);
    }

    public void deleteJobCategory(Long id) {
        JobCategory jobCategory = jobCategoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not find by this id " + id));
        jobCategoryRepository.delete(jobCategory);
    }

    public Optional<JobCategory> getCategoryById(Long id) {
        Optional<JobCategory> jobCategory = jobCategoryRepository.findById(id);

        return jobCategory;
    }

    public List<JobCategoryDTO> getAllCategory() {
        return jobCategoryRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public JobCategoryDTO mapToDTO(JobCategory jobCategory) {
        return JobCategoryDTO.builder()
                .name(jobCategory.getName())
                .build();
    }
}
