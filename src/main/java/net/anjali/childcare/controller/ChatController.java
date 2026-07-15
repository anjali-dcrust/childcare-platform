package net.anjali.childcare.controller;


import net.anjali.childcare.dto.response.ChatMessageResponse;
import net.anjali.childcare.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // Load past messages when chat screen opens
    @GetMapping("/{bookingId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(chatService.getChatHistory(bookingId));
    }
}