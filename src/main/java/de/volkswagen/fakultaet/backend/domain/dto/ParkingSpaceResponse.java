package de.volkswagen.fakultaet.backend.domain.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class ParkingSpaceResponse {
    private Long id;
    private String name;
    private String location;
    private String parkingDescription;
    private String accessInformation;
    private Double pricePerHour;
    private int amountOfParkingLots;
    private List<String> pictureUris;
}
