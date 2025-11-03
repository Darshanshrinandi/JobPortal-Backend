package com.jobPortal.Repository;

import com.jobPortal.Model.Application;
import com.jobPortal.Model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview,Long> {



    List<Interview> findByApplicationApplicationId(Long applicationId);


    List<Interview> findByStatusIgnoreCase(String status);

}
