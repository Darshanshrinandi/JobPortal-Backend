package com.jobPortal.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "company")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;


    private String name;



    @Column(unique = true, nullable = false)
    private String email;


    private String password;


    private String description;


    private String webSite;



    private String location;


    private String logo;

    private Date createdAt = new Date();
    private Date updatedAt;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<Job> jobs = new ArrayList<>();

    @OneToMany(mappedBy = "company")
    @JsonManagedReference
    private List<Review> reviews = new ArrayList<>();

    private String status ="INACTIVE";


}
