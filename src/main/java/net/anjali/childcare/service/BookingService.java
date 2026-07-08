package net.anjali.childcare.service;

import net.anjali.childcare.dto.request.BookingRequest;
import net.anjali.childcare.dto.response.BookingResponse;
import net.anjali.childcare.enums.BookingStatus;
import net.anjali.childcare.exception.ResourceNotfoundException;
import net.anjali.childcare.model.AvailabilitySlot;
import net.anjali.childcare.model.Booking;
import net.anjali.childcare.model.CaregiverProfile;
import net.anjali.childcare.model.User;
import net.anjali.childcare.repository.AvailabilitySlotRepository;
import net.anjali.childcare.repository.BookingRepository;
import net.anjali.childcare.repository.CaregiverRepository;
import net.anjali.childcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaregiverRepository caregiverRepository;

    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

    // Parent creates a booking
    public BookingResponse createBooking(String parentEmail, BookingRequest request) {

        // Get parent
        User parent = userRepository.findByEmail(parentEmail)
                .orElseThrow(() -> new ResourceNotfoundException("Parent not found"));

        // Get caregiver profile
        CaregiverProfile caregiver = caregiverRepository.findById(request.getCaregiverId())
                .orElseThrow(() -> new ResourceNotfoundException("Caregiver not found"));

        // Get slot
        AvailabilitySlot slot = availabilitySlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotfoundException("Slot not found"));

        // Check slot belongs to caregiver
        if (!slot.getCaregiver().getId().equals(caregiver.getId())) {
            throw new RuntimeException("Slot does not belong to this caregiver");
        }

        // Check slot is not already booked
        if (slot.getIsBooked()) {
            throw new RuntimeException("Slot is already booked");
        }

        // Check no active booking exists for this slot
        if (bookingRepository.existsBySlotIdAndStatusNot(slot.getId(), BookingStatus.CANCELLED)) {
            throw new RuntimeException("Slot is already reserved");
        }

        // Calculate duration in hours
        long minutes = Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
        BigDecimal durationHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60));

        // Calculate total amount
        BigDecimal totalAmount = caregiver.getHourlyRate() != null
                ? caregiver.getHourlyRate().multiply(durationHours)
                : BigDecimal.ZERO;

        // Create booking
        Booking booking = Booking.builder()
                .parent(parent)
                .caregiver(caregiver)
                .slot(slot)
                .status(BookingStatus.PENDING)
                .durationHours(durationHours)
                .totalAmount(totalAmount)
                .notes(request.getNotes())
                .build();

        bookingRepository.save(booking);

        // Lock the slot
        slot.setIsBooked(true);
        availabilitySlotRepository.save(slot);

        return mapToResponse(booking);
    }

    // Parent views own bookings
    public List<BookingResponse> getMyBookingsAsParent(String email) {
        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotfoundException("User not found"));

        return bookingRepository.findByParentId(parent.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Caregiver views own bookings
    public List<BookingResponse> getMyBookingsAsCaregiver(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotfoundException("User not found"));

        CaregiverProfile profile = caregiverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotfoundException("Caregiver profile not found"));

        return bookingRepository.findByCaregiverId(profile.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Caregiver accepts booking
    public BookingResponse acceptBooking(String email, Long bookingId) {
        Booking booking = getBookingAndValidateCaregiver(email, bookingId);

        if (!booking.getStatus().equals(BookingStatus.PENDING)) {
            throw new RuntimeException("Only pending bookings can be accepted");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        return mapToResponse(booking);
    }

    // Cancel booking (parent or caregiver)
    public BookingResponse cancelBooking(String email, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotfoundException("Booking not found"));

        // Allow parent or caregiver to cancel
        boolean isParent = booking.getParent().getEmail().equals(email);
        boolean isCaregiver = booking.getCaregiver().getUser().getEmail().equals(email);

        if (!isParent && !isCaregiver) {
            throw new RuntimeException("Unauthorized to cancel this booking");
        }

        if (booking.getStatus().equals(BookingStatus.COMPLETED)) {
            throw new RuntimeException("Cannot cancel a completed booking");
        }

        // Release the slot
        AvailabilitySlot slot = booking.getSlot();
        slot.setIsBooked(false);
        availabilitySlotRepository.save(slot);

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        return mapToResponse(booking);
    }

    // Mark booking as completed
    public BookingResponse completeBooking(String email, Long bookingId) {
        Booking booking = getBookingAndValidateCaregiver(email, bookingId);

        if (!booking.getStatus().equals(BookingStatus.CONFIRMED)) {
            throw new RuntimeException("Only confirmed bookings can be completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        return mapToResponse(booking);
    }

    // Helper — get booking and verify caregiver owns it
    private Booking getBookingAndValidateCaregiver(String email, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotfoundException("Booking not found"));

        if (!booking.getCaregiver().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized to update this booking");
        }

        return booking;
    }

    // Map entity to response DTO
    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .parentName(booking.getParent().getName())
                .parentEmail(booking.getParent().getEmail())
                .caregiverName(booking.getCaregiver().getUser().getName())
                .caregiverEmail(booking.getCaregiver().getUser().getEmail())
                .slotDate(booking.getSlot().getSlotDate())
                .startTime(booking.getSlot().getStartTime())
                .endTime(booking.getSlot().getEndTime())
                .status(booking.getStatus())
                .durationHours(booking.getDurationHours())
                .totalAmount(booking.getTotalAmount())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}