package net.anjali.childcare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    @NotNull
    private Long bookingId;

    @NotBlank
    private String content;

    @NotBlank
    private String senderEmail;
}