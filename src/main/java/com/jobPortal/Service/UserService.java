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
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
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

    @EntityGraph(attributePaths = {"roles", "skills"})
    public UserDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return convertToDTO(user);
    }

    public String verifyUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(user.getUserId(), user.getEmail(), "USER");
    }

    public Page<UserDTO> getAllUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDTO);
    }

    public User createUser(UserDTO dto, MultipartFile resumeFile, MultipartFile profileImage)
            throws IOException, MessagingException {

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
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        if (dto.getSkillNames() != null && !dto.getSkillNames().isEmpty()) {
            Set<Skill> skills = dto.getSkillNames().stream()
                    .map(skillName -> skillRepository.findByName(skillName)
                            .orElseThrow(() -> new RuntimeException("Skill not found: " + skillName)))
                    .collect(Collectors.toSet());
            user.setSkills(skills);
        }

        handleFileUploads(user, resumeFile, profileImage);
        User savedUser = userRepository.save(user);
        emailService.sendUserWelcomeEmail(savedUser.getEmail(), savedUser.getName(), dto.getPassword());
        return savedUser;
    }
    private void handleFileUploads(User user, MultipartFile resumeFile, MultipartFile profileImage) throws IOException {
        Path profileDir = Paths.get("uploads/profilePictures");
        Path resumeDir = Paths.get("uploads/resumes");

        // Ensure folders exist
        if (!Files.exists(profileDir)) Files.createDirectories(profileDir);
        if (!Files.exists(resumeDir)) Files.createDirectories(resumeDir);

        // ✅ Handle profile picture update
        if (profileImage != null && !profileImage.isEmpty()) {
            // Delete old profile picture if it exists
            if (user.getProfilePicture() != null) {
                Path oldProfile = Paths.get(user.getProfilePicture());
                try {
                    Files.deleteIfExists(oldProfile);
                } catch (IOException e) {
                    System.err.println("⚠️ Failed to delete old profile picture: " + e.getMessage());
                }
            }

            // Save new profile picture
            String fileName = "profile_" + user.getUserId() + "_" + System.currentTimeMillis()
                    + "_" + profileImage.getOriginalFilename();
            Path filePath = profileDir.resolve(fileName);
            Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            user.setProfilePicture(filePath.toString());
        }

        // ✅ Handle resume update
        if (resumeFile != null && !resumeFile.isEmpty()) {
            // Delete old resume if it exists
            if (user.getResume() != null) {
                Path oldResume = Paths.get(user.getResume());
                try {
                    Files.deleteIfExists(oldResume);
                } catch (IOException e) {
                    System.err.println("⚠️ Failed to delete old resume: " + e.getMessage());
                }
            }

            // Save new resume
            String resumeName = "resume_" + user.getUserId() + "_" + System.currentTimeMillis()
                    + "_" + resumeFile.getOriginalFilename();
            Path resumePath = resumeDir.resolve(resumeName);
            Files.copy(resumeFile.getInputStream(), resumePath, StandardCopyOption.REPLACE_EXISTING);
            user.setResume(resumePath.toString());
        }
    }


    private String saveFile(MultipartFile file, String subDir) throws IOException {
        File dir = new File(UPLOAD_DIR + subDir);
        if (!dir.exists()) dir.mkdirs();
        String filePath = dir.getAbsolutePath() + File.separator +
                System.currentTimeMillis() + "_" + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        return filePath;
    }

    public UrlResource getResume(Long id) throws MalformedURLException {
        User user = getUserOrThrow(id);
        Path path = Paths.get(user.getResume());
        validateFileExists(path, "Resume", id);
        return new UrlResource(path.toUri());
    }

    public UrlResource getProfilePicture(Long id) throws MalformedURLException {
        User user = getUserOrThrow(id);
        Path path = Paths.get(user.getProfilePicture());
        validateFileExists(path, "Profile picture", id);
        return new UrlResource(path.toUri());
    }

    private void validateFileExists(Path path, String fileType, Long id) {
        if (!Files.exists(path)) {
            throw new RuntimeException(fileType + " file not found for user " + id);
        }
    }

    public User updateUser(Long userId, UserDTO userDTO, MultipartFile resumeFile, MultipartFile profileImage)
            throws IOException {

        User existingUser = getUserOrThrow(userId);

        // Update basic fields
        if (userDTO.getName() != null) existingUser.setName(userDTO.getName());
        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getExperience() != null) existingUser.setExperience(userDTO.getExperience());

        // Update password only if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Update skills and other relations
        updateUserRelations(existingUser, userDTO);

        // Handle resume and profile picture replacement
        handleFileUploads(existingUser, resumeFile, profileImage);

        return userRepository.save(existingUser);
    }



    public void deleteUser(Long id) {
        User user = getUserOrThrow(id);
        user.getRoles().clear();
        user.getSkills().clear();
        deleteFile(user.getResume());
        deleteFile(user.getProfilePicture());
        userRepository.delete(user);
    }

    private void deleteFile(String pathStr) {
        if (pathStr != null && !pathStr.isEmpty()) {
            try {
                Files.deleteIfExists(Path.of(pathStr));
            } catch (IOException e) {
                log.warn("Could not delete file: {}", pathStr, e);
            }
        }
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    private void updateUserRelations(User user, UserDTO dto) {
        if (dto.getRoleNames() != null) {
            user.setRoles(dto.getRoleNames().stream()
                    .map(r -> roleRepository.findByName(r)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + r)))
                    .collect(Collectors.toSet()));
        }

        if (dto.getSkillNames() != null) {
            user.setSkills(dto.getSkillNames().stream()
                    .map(s -> skillRepository.findByName(s)
                            .orElseThrow(() -> new RuntimeException("Skill not found: " + s)))
                    .collect(Collectors.toSet()));
        }
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .experience(user.getExperience())
                .profilePicture(user.getProfilePicture())
                .resume(user.getResume())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .skillNames(user.getSkills().stream().map(Skill::getName).collect(Collectors.toSet()))
                .build();
    }
}
