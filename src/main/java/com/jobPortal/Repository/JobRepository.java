package com.jobPortal.Repository;

import com.jobPortal.Model.Company;
import com.jobPortal.Model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository  extends JpaRepository<Job,Long> {

    List<Job> findByCompany(Company company);

    List<Job> findByCompanyCompanyId(Long companyId);

    List<Job> findByCategoryCategoryId(Long categoryId);

    List<Job> findByStatusIgnoreCase(String status);

    List<Job> findByLocationIgnoreCaseContaining(String location);

    List<Job> findByJobTypeIgnoreCaseContaining(String jobType);

    List<Job> findByPostedDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT j FROM Job j WHERE j.postedDate >= :lastWeekDate")
    List<Job> findJobsPostedLastWeek(@Param("lastWeekDate") LocalDate lastWeekDate);

    @Query("SELECT DISTINCT j FROM Job j LEFT JOIN j.skills s " +
            "WHERE (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(j.requirement) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<Job> searchByTitleDescriptionSkillsOrLocation(@Param("keyword") String keyword,
                                                       @Param("location") String location);

}
