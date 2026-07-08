package net.anjali.childcare.controller;



import jakarta.validation.Valid;

import net.anjali.childcare.dto.request.BookingRequest;
import net.anjali.childcare.dto.response.BookingResponse;
import net.anjali.childcare.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Parent creates booking
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Principal principal) {
        return ResponseEntity.ok(
                bookingService.createBooking(principal.getName(), request));
    }

    // Parent views own bookings
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookingsAsParent(
            Principal principal) {
        return ResponseEntity.ok(
                bookingService.getMyBookingsAsParent(principal.getName()));
    }

    // Caregiver views own bookings
    @GetMapping("/caregiver/my")
    public ResponseEntity<List<BookingResponse>> getMyBookingsAsCaregiver(
            Principal principal) {
        return ResponseEntity.ok(
                bookingService.getMyBookingsAsCaregiver(principal.getName()));
    }

    // Caregiver accepts booking
    @PutMapping("/{id}/accept")
    public ResponseEntity<BookingResponse> acceptBooking(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(
                bookingService.acceptBooking(principal.getName(), id));
    }

    // Parent or caregiver cancels booking
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(
                bookingService.cancelBooking(principal.getName(), id));
    }

    // Caregiver marks booking complete
    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(
                bookingService.completeBooking(principal.getName(), id));
    }
}