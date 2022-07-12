package de.volkswagen.fakultaet.backend.service;

import de.volkswagen.fakultaet.backend.domain.model.ParkingLot;
import de.volkswagen.fakultaet.backend.domain.model.ParkingSpace;
import de.volkswagen.fakultaet.backend.domain.model.Timeslot;
import de.volkswagen.fakultaet.backend.repository.ParkingLotRepository;
import de.volkswagen.fakultaet.backend.repository.ParkingSpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    ParkingSpaceRepository parkingSpaceRepository;
    @Mock
    ParkingLotRepository parkingLotRepository;

    private ParkingService parkingService;

    private AzureBlobService azureBlobService;

    @BeforeEach
    void setUp() {
        this.parkingService = new ParkingService(this.parkingSpaceRepository, parkingLotRepository, azureBlobService);
    }

    @Test
    void getParkingSpace_searchCity_returnList() {
        // GIVEN
        ParkingSpace dummyParkingSpaceKassel = new ParkingSpace(
                null,
                "private",
                "Goethestr. 4, 34119 Kassel",
                "some parking description",
                "some access information",
                2.5,
                new ArrayList<>(),
                null,
                null,
                null
        );
        ParkingLot dummyParkingLotKassel = new ParkingLot();
        dummyParkingLotKassel.getReservedTimeslots()
                .add(new Timeslot(
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1)
                ));
        dummyParkingSpaceKassel.getParkingLots().add(dummyParkingLotKassel);
        // WHEN
        when(this.parkingSpaceRepository.findByLocationContainsIgnoreCase(anyString()))
                .thenReturn(Arrays.asList(dummyParkingSpaceKassel));
        //List<ParkingSpace> parkingSpaceList = this.parkingService.getParkingSpacesByLocation("kassel");
        // THEN
        //assertThat(parkingSpaceList).isNotNull();
        //assertThat(parkingSpaceList.isEmpty()).isFalse();
    }
}