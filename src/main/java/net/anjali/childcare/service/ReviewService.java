package net.anjali.childcare.service;

import net.anjali.childcare.dto.request.ReviewRequest;
import net.anjali.childcare.dto.response.ReviewResponse;
import net.anjali.childcare.enums.BookingStatus;
import net.anjali.childcare.exception.ResourceNotfoundException;
import net.anjali.childcare.model.Booking;
import net.anjali.childcare.model.CaregiverProfile;
import net.anjali.childcare.model.Review;
import net.anjali.childcare.model.User;
import net.anjali.childcare.repository.BookingRepository;
import net.anjali.childcare.repository.CaregiverRepository;
import net.anjali.childcare.repository.ReviewRepository;
import net.anjali.childcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaregiverRepository caregiverRepository;

    // Parent submits a review
    public ReviewResponse submitReview(String parentEmail, ReviewRequest request) {

        // Get parent
        User parent = userRepository.findByEmail(parentEmail)
                .orElseThrow(() -> new ResourceNotfoundException("User not found"));

        // Get booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotfoundException("Booking not found"));

        // Verify the parent owns this booking
        if (!booking.getParent().getId().equals(parent.getId())) {
            throw new RuntimeException("You can only review your own bookings");
        }

        // Only completed bookings can be reviewed
        if (!booking.getStatus().equals(BookingStatus.COMPLETED)) {
            throw new RuntimeException("You can only review completed bookings");
        }

        // Check review doesn't already exist for this booking
        if (reviewRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new RuntimeException("You have already reviewed this booking");
        }

        // Get caregiver
        CaregiverProfile caregiver = booking.getCaregiver();

        // Create review
        Review review = Review.builder()
                .booking(booking)
                .parent(parent)
                .caregiver(caregiver)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        // Update caregiver average rating
        updateCaregiverRating(caregiver);

        return mapToResponse(review);
    }

    // Get all reviews for a caregiver
    public List<ReviewResponse> getCaregiverReviews(Long caregiverId) {
        caregiverRepository.findById(caregiverId)
                .orElseThrow(() -> new ResourceNotfoundException("Caregiver not found"));

        return reviewRepository.findByCaregiverId(caregiverId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Recalculate and update caregiver average rating
    private void updateCaregiverRating(CaregiverProfile caregiver) {
        Double avg = reviewRepository.findAverageRatingByCaregiverId(caregiver.getId());

        if (avg != null) {
            caregiver.setAverageRating(
                    BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
            );
            caregiverRepository.save(caregiver);
        }
    }

    // Map entity to response DTO
    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .parentName(review.getParent().getName())
                .caregiverName(review.getCaregiver().getUser().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}