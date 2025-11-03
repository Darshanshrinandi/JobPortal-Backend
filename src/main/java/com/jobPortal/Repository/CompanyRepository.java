package com.jobPortal.Repository;

import com.jobPortal.Model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {

    Optional<Company> findByEmail(String email);
    boolean existsByEmail(String email);


    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.location) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Company> searchByKeyword(@Param("keyword") String keyword);


}
