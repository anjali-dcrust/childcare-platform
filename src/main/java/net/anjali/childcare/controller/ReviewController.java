package net.anjali.childcare.controller;

import jakarta.validation.Valid;
import net.anjali.childcare.dto.request.ReviewRequest;
import net.anjali.childcare.dto.response.ReviewResponse;
import net.anjali.childcare.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Parent submits a review
    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(
            @Valid @RequestBody ReviewRequest request,
            Principal principal) {
        return ResponseEntity.ok(
                reviewService.submitReview(principal.getName(), request));
    }

    // Anyone views caregiver reviews
    @GetMapping("/caregiver/{id}")
    public ResponseEntity<List<ReviewResponse>> getCaregiverReviews(
            @PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getCaregiverReviews(id));
    }
}