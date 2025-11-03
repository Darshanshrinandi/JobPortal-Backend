package com.jobPortal.Service;

import com.jobPortal.Jwt.JwtService;
import com.jobPortal.DTO.UserDTO;
import com.jobPortal.Model.Role;
import com.jobPortal.Model.Skill;
import com.jobPortal.Model.User;
import com.jobPortal.Repository.RoleRepository;
import com.jobPortal.Repository.SkillRepository;
import com.jobPortal.Repository.UserRepository;
import jakarta.annotation.PostConstruct;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class UserService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmailService emailService;


    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private JwtService jwtService;

    @Value("${uploads.dir}")
    private String UPLOAD_DIR;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    public User findUserById(Long id) {

        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found by this id " + id));
    }

    public String verifyUser(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(email);
        } else {
            throw new RuntimeException("Authentication Failed");
        }
    }


    @Transactional
    public Page<UserDTO> getAllUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<User> users = userRepository.findAll(pageable);
        return users.map(user -> UserDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .experience(user.getExperience())
                .roleNames(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .skillNames(user.getSkills().stream().map(s -> s.getName()).collect(Collectors.toSet()))
                .resume(user.getResume())
                .profilePicture(user.getProfilePicture())
                .build()
        );
    }

    @Transactional
    public User createUser(UserDTO dto,
                           MultipartFile resumeFile,
                           MultipartFile profileImage) throws IOException, MessagingException {

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .experience(dto.getExperience())
                .applications(new ArrayList<>())
                .savedJobs(new ArrayList<>())
                .reviews(new ArrayList<>())
                .build();

        if (dto.getRoleNames() != null && !dto.getRoleNames().isEmpty()) {
            Set<Role> roles = dto.getRoleNames().stream()
                    .map(name -> roleRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + name)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        if (dto.getSkillNames() != null && !dto.getSkillNames().isEmpty()) {
            Set<Skill> skills = dto.getSkillNames().stream()
                    .map(name -> skillRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException("Skill not found: " + name)))
                    .collect(Collectors.toSet());
            user.setSkills(skills);
        }


        List<Path> createdFiles = new ArrayList<>();

        try {
            // Handle resume file
            if (resumeFile != null && !resumeFile.isEmpty()) {
                File resumeDir = new File(UPLOAD_DIR + "resumes");
                if (!resumeDir.exists()) resumeDir.mkdirs();
                String resumePath = resumeDir.getAbsolutePath() + File.separator
                        + System.currentTimeMillis() + "_" + resumeFile.getOriginalFilename();
                resumeFile.transferTo(new File(resumePath));
                user.setResume(resumePath);
                createdFiles.add(Path.of(resumePath));
            }


            if (profileImage != null && !profileImage.isEmpty()) {
                File profileDir = new File(UPLOAD_DIR + "profile_images");
                if (!profileDir.exists()) profileDir.mkdirs();
                String profilePath = profileDir.getAbsolutePath() + File.separator
                        + System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
                profileImage.transferTo(new File(profilePath));
                user.setProfilePicture(profilePath);
                createdFiles.add(Path.of(profilePath));
            }


            User savedUser = userRepository.save(user);

            emailService.sendUserWelcomeEmail(savedUser.getEmail(), savedUser.getName(), savedUser.getPassword());

            return savedUser;

        } catch (Exception e) {

            for (Path path : createdFiles) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            }
            throw e;
        }
    }


    @Transactional
    public UrlResource getResume(Long id) throws MalformedURLException {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found by this id " + id));
        Path path = Paths.get(user.getResume());
        if (!Files.exists(path)) {
            throw new RuntimeException("Resume file not found " + id);
        }
        return new UrlResource(path.toUri());
    }

    @Transactional
    public UrlResource getProfilePicture(Long id) throws MalformedURLException {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found by this id " + id));

        Path path = Paths.get(user.getProfilePicture());

        if (!Files.exists(path)) {
            throw new RuntimeException("Profile picture file not found " + id);

        }

        return new UrlResource(path.toUri());
    }

    @Transactional
    public User updateUser(Long userId,
                           UserDTO userDTO,
                           MultipartFile resumeFile,
                           MultipartFile profileImage) throws IOException {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found by this id " + userId));

        // Update basic fields
        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        existingUser.setExperience(userDTO.getExperience());

        // Update roles
        if (userDTO.getRoleNames() != null && !userDTO.getRoleNames().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : userDTO.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            }
            existingUser.getRoles().clear();
            existingUser.getRoles().addAll(roles);
        }

        // Update skills
        if (userDTO.getSkillNames() != null && !userDTO.getSkillNames().isEmpty()) {
            Set<Skill> skills = new HashSet<>();
            for (String skillName : userDTO.getSkillNames()) {
                Skill skill = skillRepository.findByName(skillName)
                        .orElseThrow(() -> new RuntimeException("Skill not found: " + skillName));
                skills.add(skill);
            }
            existingUser.getSkills().clear();
            existingUser.getSkills().addAll(skills);
        }

        // Update resume file
        if (resumeFile != null && !resumeFile.isEmpty()) {
            File resumeDir = new File(UPLOAD_DIR + "resumes");
            if (!resumeDir.exists()) resumeDir.mkdirs();

            String resumePath = resumeDir.getAbsolutePath() + File.separator
                    + System.currentTimeMillis() + "_" + resumeFile.getOriginalFilename();
            resumeFile.transferTo(new File(resumePath));
            existingUser.setResume(resumePath);
        }

        // Update profile image
        if (profileImage != null && !profileImage.isEmpty()) {
            File profileDir = new File(UPLOAD_DIR + "profile_images");
            if (!profileDir.exists()) profileDir.mkdirs();

            String profilePath = profileDir.getAbsolutePath() + File.separator
                    + System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
            profileImage.transferTo(new File(profilePath));
            existingUser.setProfilePicture(profilePath);
        }

        return userRepository.save(existingUser);
    }


    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found by this id " + id));

        user.getRoles().clear();
        user.getSkills().clear();

        if (user.getResume() != null && !user.getResume().isEmpty()) {
            try {
                Files.deleteIfExists(Path.of(user.getResume()));
            } catch (IOException e) {
                log.error("Failed to delete resume for the user " + user.getUserId() + " " + e.getMessage());
            }
        }

        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            try {
                Files.deleteIfExists(Path.of(user.getProfilePicture()));
            } catch (IOException e) {
                log.error("Failed to delete resume for the user " + user.getUserId() + " " + e.getMessage());
            }
        }

        userRepository.delete(user);
    }


}
