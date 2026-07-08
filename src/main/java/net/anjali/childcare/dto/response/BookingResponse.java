package net.anjali.childcare.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.anjali.childcare.enums.BookingStatus;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter @Setter
@Builder
public class BookingResponse {

    private Long id;

    // Parent info
    private String parentName;
    private String parentEmail;

    // Caregiver info
    private String caregiverName;
    private String caregiverEmail;

    // Slot info
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Booking info
    private BookingStatus status;
    private BigDecimal durationHours;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
}