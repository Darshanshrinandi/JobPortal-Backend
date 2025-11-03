package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.ReviewDTO;
import com.jobPortal.Service.ReviewService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobPortal/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        ReviewDTO review = reviewService.addReview(reviewDTO);
        ApiResponse<ReviewDTO> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Review Added",
                review
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("#reviewDTO.userId == authentication.principal.id or hasRole('ADMIN')")
    @PutMapping("/updateReview/{id}")
    public ResponseEntity<ApiResponse<ReviewDTO>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        ReviewDTO updatedReview = reviewService.updateReview(id, reviewDTO);
        ApiResponse<ReviewDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Review Updated",
                updatedReview
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("#reviewDTO.userId == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Review Deleted",
                null
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getAllReviews() {
        List<ReviewDTO> reviews = reviewService.getAllReviews();
        ApiResponse<List<ReviewDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Review List",
                reviews
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PermitAll
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewDTO>> getReviewById(@PathVariable Long id) {
        ReviewDTO review = reviewService.getReviewById(id);
        ApiResponse<ReviewDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Review Details",
                review
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
