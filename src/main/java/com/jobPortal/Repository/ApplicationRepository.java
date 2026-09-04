package com.jobPortal.Repository;

import com.jobPortal.Model.Application;
import com.jobPortal.Model.Job;
import com.jobPortal.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application,Long> {

      List<Application> findByUser(User user);
      List<Application> findByJob(Job job);

      Boolean existsByUserUserIdAndJobJobId(Long userId, Long jobId);

      List<Application> findByUserUserId(Long userId);

      List<Application> findByJobJobId(Long jobId);

    boolean existsByUser_UserIdAndJob_JobId(Long userId, Long jobId);

    @Query("""
    SELECT a
    FROM Application a
    JOIN a.user u
    JOIN a.job j
    JOIN j.company c
    WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Application> searchApplications(@Param("keyword") String keyword);


}
