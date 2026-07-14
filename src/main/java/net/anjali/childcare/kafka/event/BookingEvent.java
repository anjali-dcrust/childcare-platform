package net.anjali.childcare.kafka.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.anjali.childcare.enums.BookingStatus;


import java.io.Serializable;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class BookingEvent implements Serializable {

    private Long bookingId;
    private String parentEmail;
    private String parentName;
    private String caregiverEmail;
    private String caregiverName;
    private BookingStatus status;
    private String message;
}