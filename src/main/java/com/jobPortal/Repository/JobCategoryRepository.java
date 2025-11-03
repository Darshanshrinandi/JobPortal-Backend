package com.jobPortal.Repository;

import com.jobPortal.Model.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface    JobCategoryRepository extends JpaRepository<JobCategory,Long> {

    Optional<JobCategory> findByName(String name);
    Boolean existsByNameIgnoreCase(String name);
}
