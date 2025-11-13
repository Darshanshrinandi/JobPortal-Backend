package com.jobPortal.Service;

import com.jobPortal.DTO.ReviewDTO;
import com.jobPortal.Model.Company;
import com.jobPortal.Model.Review;
import com.jobPortal.Model.User;
import com.jobPortal.Repository.CompanyRepository;
import com.jobPortal.Repository.ReviewRepository;
import com.jobPortal.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    public ReviewDTO addReview(ReviewDTO reviewDTO) {
        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id " + reviewDTO.getUserId()));

        Company company = companyRepository.findById(reviewDTO.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with id " + reviewDTO.getCompanyId()));

        Review review = Review.builder()
                .user(user)
                .company(company)
                .ratings(reviewDTO.getRatings())
                .comment(reviewDTO.getComment())
                .createdAt(new Date())
                .build();

        review = reviewRepository.save(review);

        return mapToDTO(review);
    }

    public ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDTO) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id " + reviewId));

        review.setRatings(reviewDTO.getRatings());
        review.setComment(reviewDTO.getComment());
        review.setCreatedAt(new Date());

        review = reviewRepository.save(review);

        return mapToDTO(review);
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id " + reviewId));
        reviewRepository.delete(review);
    }

    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id " + id));
        return mapToDTO(review);
    }

    public List<ReviewDTO> getReviewsByCompany(Long companyId) {
        return reviewRepository.findByCompany_CompanyId(companyId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    private ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUser().getUserId())
                .companyId(review.getCompany().getCompanyId())
                .ratings(review.getRatings())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
