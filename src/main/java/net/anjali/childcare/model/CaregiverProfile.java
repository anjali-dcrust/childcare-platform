package net.anjali.childcare.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "caregiver_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CaregiverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String bio;

    @Column(precision = 6, scale = 2)
    private BigDecimal hourlyRate;

    private Integer experienceYears;

    private String specializations;     // e.g. "infants,toddlers"

    @Column(nullable = false)
    private Boolean isVerified = false;

    private String docUrl;

    private String city;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    // Relationships
    @OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL)
    private List<AvailabilitySlot> availabilitySlots;

    @OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL)
    private List<Review> reviews;
}