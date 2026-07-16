package net.anjali.childcare.model;

import jakarta.persistence.*;
import lombok.*;
import net.anjali.childcare.enums.BookingStatus;
import net.anjali.childcare.enums.SessionStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = false)
    private CaregiverProfile caregiver;

    @OneToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private AvailabilitySlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(precision = 4, scale = 2)
    private BigDecimal durationHours;

    @Column(precision = 8, scale = 2)
    private BigDecimal totalAmount;

    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Relationship
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Review review;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus sessionStatus = SessionStatus.NOT_STARTED;
}