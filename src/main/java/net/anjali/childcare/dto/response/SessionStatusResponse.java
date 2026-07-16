package net.anjali.childcare.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.anjali.childcare.enums.SessionStatus;


import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class SessionStatusResponse {

    private Long bookingId;
    private SessionStatus sessionStatus;
    private LocalDateTime updatedAt;
}