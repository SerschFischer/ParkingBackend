package de.volkswagen.fakultaet.backend.domain.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PARKING_LOTS")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ParkingLot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(targetEntity = ParkingSpace.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "PARKING_SPACE_PARKING_LOT_ID", referencedColumnName = "id")
    private ParkingSpace parkingSpace;
    @ElementCollection
    @CollectionTable(name = "RESERVED_TIMESLOTS",
            joinColumns = @JoinColumn(name = "RESERVED_TIMESLOT_ID"))
    private List<Timeslot> reservedTimeslots = new ArrayList<>();
}
