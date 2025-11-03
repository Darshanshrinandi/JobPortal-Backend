package com.jobPortal.Model;



import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import lombok.*;

import java.util.*;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;


    private String name;


    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false)
    private String password;

    private String profilePicture;
    ;

    private String resume;

    private String experience;

    private Date createdAt;

    private Date updatedAt;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(

            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )

    private Set<Role> roles;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(

            name = "user_skill",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-applications")
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<SavedJobs> savedJobs = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-reviews")
    private List<Review> reviews = new ArrayList<>();

    @ElementCollection
    private Set<String> preferredLocations = new HashSet<>();

    @ElementCollection
    private Set<String> preferredCategories = new HashSet<>();

}
