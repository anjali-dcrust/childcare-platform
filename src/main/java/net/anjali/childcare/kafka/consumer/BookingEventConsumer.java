package net.anjali.childcare.kafka.consumer;

import net.anjali.childcare.kafka.event.BookingEvent;
import net.anjali.childcare.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventConsumer.class);

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "booking-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBookingEvent(BookingEvent event) {
        logger.info("📩 Received booking event: {}", event);

        String subject = buildSubject(event);

        // Notify parent
        emailService.sendEmail(
                event.getParentEmail(),
                subject,
                buildEmailBody(event, true)
        );

        // Notify caregiver
        emailService.sendEmail(
                event.getCaregiverEmail(),
                subject,
                buildEmailBody(event, false)
        );
    }

    private String buildSubject(BookingEvent event) {
        return switch (event.getStatus()) {
            case PENDING -> "New Booking Request - Childcare Platform";
            case CONFIRMED -> "Booking Confirmed - Childcare Platform";
            case CANCELLED -> "Booking Cancelled - Childcare Platform";
            case COMPLETED -> "Booking Completed - Childcare Platform";
        };
    }

    private String buildEmailBody(BookingEvent event, boolean isParent) {
        String name = isParent ? event.getParentName() : event.getCaregiverName();
        String otherParty = isParent ? event.getCaregiverName() : event.getParentName();

        return "Hi " + name + ",\n\n"
                + event.getMessage() + "\n\n"
                + "Booking ID: " + event.getBookingId() + "\n"
                + "With: " + otherParty + "\n"
                + "Status: " + event.getStatus() + "\n\n"
                + "Thank you for using our Childcare Platform.";
    }
}