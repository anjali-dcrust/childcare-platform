package net.anjali.childcare.kafka.consumer;


import net.anjali.childcare.kafka.event.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventConsumer.class);

    @KafkaListener(topics = "booking-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBookingEvent(BookingEvent event) {
        logger.info("📩 Received booking event: {}", event);
        logger.info("Booking ID: {} | Status: {} | Message: {}",
                event.getBookingId(), event.getStatus(), event.getMessage());

        // Email logic will be added here in next step
    }
}