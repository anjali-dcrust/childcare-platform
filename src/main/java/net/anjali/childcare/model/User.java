package net.anjali.childcare.model;

import jakarta.persistence.*;
import lombok.*;
import net.anjali.childcare.enums.Role;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Relationships
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private CaregiverProfile caregiverProfile;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Booking> bookings;
}