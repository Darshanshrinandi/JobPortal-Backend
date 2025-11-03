package com.jobPortal.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Date scheduledDate;


    private String mode;


    private String status;


    private String feedback;

}
