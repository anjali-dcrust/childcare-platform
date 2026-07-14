package net.anjali.childcare.kafka.producer;

import net.anjali.childcare.kafka.event.BookingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingEventProducer {

    private static final String TOPIC = "booking-events";

    @Autowired
    private KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public void publishBookingEvent(BookingEvent event) {
        kafkaTemplate.send(TOPIC, event.getBookingId().toString(), event);
    }
}