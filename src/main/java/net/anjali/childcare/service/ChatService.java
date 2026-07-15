package net.anjali.childcare.service;

import net.anjali.childcare.dto.request.ChatMessageRequest;
import net.anjali.childcare.dto.response.ChatMessageResponse;
import net.anjali.childcare.exception.ResourceNotfoundException;
import net.anjali.childcare.model.Booking;
import net.anjali.childcare.model.ChatMessage;
import net.anjali.childcare.model.User;
import net.anjali.childcare.repository.BookingRepository;
import net.anjali.childcare.repository.ChatMessageRepository;
import net.anjali.childcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatMessageResponse saveMessage(String senderEmail,
                                           ChatMessageRequest request) {

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() ->
                        new ResourceNotfoundException("User not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() ->
                        new ResourceNotfoundException("Booking not found"));

        boolean isParent =
                booking.getParent().getId().equals(sender.getId());

        boolean isCaregiver =
                booking.getCaregiver()
                        .getUser()
                        .getId()
                        .equals(sender.getId());

        if (!isParent && !isCaregiver) {
            throw new RuntimeException(
                    "You are not part of this booking."
            );
        }

        ChatMessage message = ChatMessage.builder()
                .booking(booking)
                .sender(sender)
                .content(request.getContent())
                .build();

        message = chatMessageRepository.save(message);

        return ChatMessageResponse.builder()
                .id(message.getId())
                .bookingId(message.getBooking().getId())
                .senderEmail(sender.getEmail())
                .senderName(sender.getName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }

    public List<ChatMessageResponse> getChatHistory(Long bookingId) {

        return chatMessageRepository
                .findByBookingIdOrderBySentAtAsc(bookingId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {

        return ChatMessageResponse.builder()
                .id(message.getId())
                .bookingId(message.getBooking().getId())
                .senderEmail(message.getSender().getEmail())
                .senderName(message.getSender().getName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }
}