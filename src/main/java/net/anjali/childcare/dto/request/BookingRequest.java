package net.anjali.childcare.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookingRequest {

    @NotNull(message = "Caregiver ID is required")
    private Long caregiverId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    private String notes;
}