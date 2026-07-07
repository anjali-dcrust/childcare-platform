package net.anjali.childcare.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
@Builder
public class CaregiverResponse {

    private Long id;
    private String name;
    private String email;
    private String bio;
    private BigDecimal hourlyRate;
    private Integer experienceYears;
    private String specializations;
    private String city;
    private Boolean isVerified;
    private BigDecimal averageRating;
}