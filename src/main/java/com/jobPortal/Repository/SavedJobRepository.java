package com.jobPortal.Repository;

import com.jobPortal.Model.SavedJobs;
import com.jobPortal.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJobs, Long> {

    List<SavedJobs> findByUser(User user);

    Optional<SavedJobs> findByUserUserIdAndJobJobId(Long userId, Long jobId);

    boolean existsByUserUserIdAndJobJobId(Long userId, Long jobId);
}

