package com.jobPortal.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Objects;

@Entity
@Table(name = "saved_jobs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavedJobs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    @JsonBackReference
    private Job job;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SavedJobs savedJobs = (SavedJobs) o;
        return Objects.equals(id, savedJobs.id) && Objects.equals(user, savedJobs.user) && Objects.equals(job, savedJobs.job);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, job);
    }
}
