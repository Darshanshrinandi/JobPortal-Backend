package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.UserDTO;
import com.jobPortal.Model.User;
import com.jobPortal.Service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/JobPortal/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("#id==authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/findUser/id/{id}")
    public ResponseEntity<ApiResponse<User>> findUserById(@PathVariable Long id) {

        User user = userService.findUserById(id);

        ApiResponse<User> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User fecthed Successfully",
                user
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/findAllUsers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> findAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        Page<UserDTO> users = userService.getAllUsers(page, size, sortBy);

        Map<String, Object> responseData = new HashMap<>();

        responseData.put("users", users);
        responseData.put("currentPage", users.getNumber());
        responseData.put("totalPages", users.getTotalPages());
        responseData.put("totalItems", users.getTotalElements());

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Users fetched successfully",
                responseData
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PreAuthorize("#id==authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/resume/{id}/info")
    public ResponseEntity<ApiResponse<String>> getResumeInfo(@PathVariable Long id) {
        User user = userService.findUserById(id);

        if (user.getResume() == null && user.getResume().isEmpty()) {
            ApiResponse<String> response = new ApiResponse<>(
                    404,
                    "resume not found by this id " + id,
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        String url = "/users/resume/" + id + "/download";

        ApiResponse<String> response = new ApiResponse<>(
                200,
                "resume is ready for download",
                url
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PreAuthorize("#id==authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/resume/{id}/download")
    public ResponseEntity<UrlResource> downloadResume(@PathVariable Long id) throws IOException {
        UrlResource urlResource = userService.getResume(id);

        String url = Paths.get(urlResource.getURI()).getFileName().toString();

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String mimeType = Files.probeContentType(Paths.get(urlResource.getURI()));
            if (mimeType != null) {
                mediaType = MediaType.parseMediaType(mimeType);
            }
        } catch (IOException ignored) {

        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + url + "\"")
                .body(urlResource);


    }

    @PreAuthorize("#id==authentication.principal.id or hasRole('ADMIN')")
    @PutMapping(value = "/updateUser/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id,
                                                        @Valid @RequestPart("user") UserDTO userDTO,
                                                        @RequestPart(value = "resume", required = false) MultipartFile resumeFile,
                                                        @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture
    ) throws IOException {

        User user = userService.updateUser(id, userDTO, resumeFile, profilePicture);
        ApiResponse<User> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User updated successfully",
                user
        );

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PreAuthorize("#id==authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/profile-picture/{id}/download")
    public ResponseEntity<UrlResource> downloadProfilePicture(@PathVariable Long id) throws IOException {
        UrlResource resource = userService.getProfilePicture(id);

        String fileName = Paths.get(resource.getURI()).getFileName().toString();

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String mimeType = Files.probeContentType(Paths.get(resource.getURI()));
            if (mimeType != null) {
                mediaType = MediaType.parseMediaType(mimeType);

            }

        } catch (IOException ignored) {
        }

        return ResponseEntity.ok().contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/id/{id}")
    public ResponseEntity<ApiResponse<User>> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
