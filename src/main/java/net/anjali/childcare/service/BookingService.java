package net.anjali.childcare.service;

import net.anjali.childcare.dto.request.BookingRequest;
import net.anjali.childcare.dto.response.BookingResponse;
import net.anjali.childcare.dto.response.SessionStatusResponse;
import net.anjali.childcare.enums.BookingStatus;
import net.anjali.childcare.enums.SessionStatus;
import net.anjali.childcare.exception.ResourceNotfoundException;
import net.anjali.childcare.kafka.event.BookingEvent;
import net.anjali.childcare.kafka.producer.BookingEventProducer;
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
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    @Autowired
    private BookingEventProducer bookingEventProducer;

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
        bookingEventProducer.publishBookingEvent(BookingEvent.builder()
                .bookingId(booking.getId())
                .parentEmail(parent.getEmail())
                .parentName(parent.getName())
                .caregiverEmail(caregiver.getUser().getEmail())
                .caregiverName(caregiver.getUser().getName())
                .status(BookingStatus.PENDING)
                .message("New booking request created")
                .build());

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
        bookingEventProducer.publishBookingEvent(BookingEvent.builder()
                .bookingId(booking.getId())
                .parentEmail(booking.getParent().getEmail())
                .parentName(booking.getParent().getName())
                .caregiverEmail(booking.getCaregiver().getUser().getEmail())
                .caregiverName(booking.getCaregiver().getUser().getName())
                .status(BookingStatus.CONFIRMED)
                .message("Booking confirmed by caregiver")
                .build());
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
        bookingEventProducer.publishBookingEvent(BookingEvent.builder()
                .bookingId(booking.getId())
                .parentEmail(booking.getParent().getEmail())
                .parentName(booking.getParent().getName())
                .caregiverEmail(booking.getCaregiver().getUser().getEmail())
                .caregiverName(booking.getCaregiver().getUser().getName())
                .status(BookingStatus.CANCELLED)
                .message("Booking has been cancelled")
                .build());
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
        bookingEventProducer.publishBookingEvent(BookingEvent.builder()
                .bookingId(booking.getId())
                .parentEmail(booking.getParent().getEmail())
                .parentName(booking.getParent().getName())
                .caregiverEmail(booking.getCaregiver().getUser().getEmail())
                .caregiverName(booking.getCaregiver().getUser().getName())
                .status(BookingStatus.COMPLETED)
                .message("Booking marked as completed")
                .build());
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
    // Caregiver updates session status during active booking
    public SessionStatusResponse updateSessionStatus(String email, Long bookingId, SessionStatus newStatus) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotfoundException("Booking not found"));

        // Only the assigned caregiver can update session status
        if (!booking.getCaregiver().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized to update this session");
        }

        // Booking must be confirmed before session can start
        if (!booking.getStatus().equals(BookingStatus.CONFIRMED)) {
            throw new RuntimeException("Booking must be confirmed before updating session status");
        }

        // Enforce valid status progression
        validateStatusTransition(booking.getSessionStatus(), newStatus);

        booking.setSessionStatus(newStatus);
        bookingRepository.save(booking);

        // Push live update to parent via WebSocket (reuses your chat infra)
        SessionStatusResponse response = SessionStatusResponse.builder()
                .bookingId(booking.getId())
                .sessionStatus(booking.getSessionStatus())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/session/" + booking.getId(), response);

        return response;
    }

    // Prevent skipping steps or going backwards
    private void validateStatusTransition(SessionStatus current, SessionStatus next) {
        java.util.Map<SessionStatus, SessionStatus> allowedNext = java.util.Map.of(
                SessionStatus.NOT_STARTED, SessionStatus.ARRIVED,
                SessionStatus.ARRIVED, SessionStatus.IN_PROGRESS,
                SessionStatus.IN_PROGRESS, SessionStatus.ENDED
        );

        if (!next.equals(allowedNext.get(current))) {
            throw new RuntimeException(
                    "Invalid status transition from " + current + " to " + next);
        }
    }
}