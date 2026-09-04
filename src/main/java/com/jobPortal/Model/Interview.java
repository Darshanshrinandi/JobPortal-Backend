package com.jobPortal.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "interview")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @JsonBackReference
    private Application application;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "scheduled_date", nullable = false)
    private Date scheduledDate;

    @Column(nullable = false)
    private String mode;

    @Column(nullable = false)
    private String status;

    private String feedback;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false, insertable = true)
    private Date createdAt;
}