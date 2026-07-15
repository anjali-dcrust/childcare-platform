package net.anjali.childcare.controller;

import net.anjali.childcare.dto.request.ChatMessageRequest;
import net.anjali.childcare.dto.response.ChatMessageResponse;
import net.anjali.childcare.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Client sends to /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request) {

        ChatMessageResponse response =
                chatService.saveMessage(request.getSenderEmail(), request);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.getBookingId(),
                response
        );
    }
}