package net.anjali.childcare.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class ChatMessageResponse {

    private Long id;
    private Long bookingId;
    private String senderEmail;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
}