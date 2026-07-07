package net.anjali.childcare.dto.request;


import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class CaregiverProfileRequest {

    private String bio;
    private BigDecimal hourlyRate;
    private Integer experienceYears;
    private String specializations;
    private String city;
}