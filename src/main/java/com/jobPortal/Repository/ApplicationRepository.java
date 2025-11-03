package com.jobPortal.Repository;

import com.jobPortal.Model.Application;
import com.jobPortal.Model.Job;
import com.jobPortal.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application,Long> {

      List<Application> findByUser(User user);
      List<Application> findByJob(Job job);

      Boolean existsByUserUserIdAndJobJobId(Long userId, Long jobId);

      List<Application> findByUserUserId(Long userId);

      List<Application> findByJobJobId(Long jobId);

}
