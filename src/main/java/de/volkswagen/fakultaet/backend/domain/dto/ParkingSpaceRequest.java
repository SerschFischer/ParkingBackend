package de.volkswagen.fakultaet.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class ParkingSpaceRequest {
    private String name;
    private String location;
    private String parkingDescription;
    private String accessInformation;
    private double pricePerHour;
    private int amountOfParkingLots;
}
