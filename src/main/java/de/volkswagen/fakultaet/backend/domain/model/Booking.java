package de.volkswagen.fakultaet.backend.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOOKINGS")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ownerId;
    private Long parkingSpaceId;
    private Long parkingLotId;
    private Long tenantId;
    private LocalDateTime arrivalDateTime;
    private LocalDateTime departureDateTime;
    private double totalPrice;
    private BookingStatus bookingStatus;
}
