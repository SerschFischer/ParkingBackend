package de.volkswagen.fakultaet.backend.service;

import com.microsoft.azure.storage.StorageException;
import com.sun.jndi.toolkit.url.Uri;
import de.volkswagen.fakultaet.backend.domain.dto.ParkingSpaceCreateRequest;
import de.volkswagen.fakultaet.backend.domain.dto.ParkingSpaceResponse;
import de.volkswagen.fakultaet.backend.domain.dto.ParkingSpaceUpdateRequest;
import de.volkswagen.fakultaet.backend.domain.model.*;
import de.volkswagen.fakultaet.backend.repository.ParkingLotRepository;
import de.volkswagen.fakultaet.backend.repository.ParkingSpaceRepository;
import de.volkswagen.fakultaet.backend.utilities.ApplicationUtilities;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingService.class);
    private ParkingSpaceRepository parkingSpaceRepository;
    private ParkingLotRepository parkingLotRepository;

    private AzureBlobService azureBlobService;

    public ParkingService(ParkingSpaceRepository parkingSpaceRepository, ParkingLotRepository parkingLotRepository, AzureBlobService azureBlobService) {
        this.parkingSpaceRepository = parkingSpaceRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.azureBlobService = azureBlobService;
    }

    public ParkingSpaceResponse createParkingSpace(ParkingSpaceCreateRequest request, User user) {
        ParkingSpace parkingSpaceDao = new ParkingSpace();
        BeanUtils.copyProperties(request, parkingSpaceDao, ApplicationUtilities.getNullPropertyNames(request));
        List<ParkingLot> parkingLots = new ArrayList<>();
        for (int i = 0; i < request.getAmountOfParkingLots(); i++) {
            parkingLots.add(new ParkingLot());
        }
        parkingSpaceDao.setParkingLots(parkingLots);
        parkingSpaceDao.setUser(user);
        return mapParkingSpaceToParkingSpaceResponse(this.parkingSpaceRepository.save(parkingSpaceDao));
    }

    public ParkingSpace getParkingSpaceById(Long id) {
        return this.parkingSpaceRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    public List<ParkingSpaceResponse> getParkingSpacesByUser(User user) {
        return this.parkingSpaceRepository.findByUser(user).stream()
                .map(parkingSpace -> mapParkingSpaceToParkingSpaceResponse(parkingSpace))
                .collect(Collectors.toList());
    }


    public List<ParkingSpaceResponse> getAvailableParkingSpacesBySearchParam(String location, LocalDateTime arrival, LocalDateTime departure) {
        if (arrival.isAfter(departure)) {
            throw new IllegalArgumentException("Starting date time should be before ending date time!");
        }
        return this.parkingSpaceRepository.findByLocationContainsIgnoreCase(location).stream()
                .filter(parkingSpace -> parkingSpace.getParkingLots() != null)
                .filter(parkingSpace -> !parkingSpace.getParkingLots().isEmpty())
                .filter(parkingSpace -> !getAvailableParkingLots(parkingSpace, arrival, departure).isEmpty())
                .map(parkingSpace -> {
                    ParkingSpaceResponse parkingSpaceResponse = new ParkingSpaceResponse();
                    BeanUtils.copyProperties(parkingSpace, parkingSpaceResponse);
                    parkingSpaceResponse.setAmountOfParkingLots(getAvailableParkingLots(parkingSpace, arrival, departure).size());
                    return parkingSpaceResponse;
                })
                .collect(Collectors.toList());
    }



    public ParkingSpaceResponse addPictureToParkingSpace(User currentUser, Long parkingSpaceId, MultipartFile multipartFile) {
        ParkingSpace parkingSpaceToUpdate = this.parkingSpaceRepository.findById(parkingSpaceId)
                .orElseThrow(EntityNotFoundException::new);
        if (!currentUser.getMyParkingSpaces().contains(parkingSpaceToUpdate)) {
            throw new NoPermissionException();
        }
        if (parkingSpaceToUpdate.getPictureUris().size() < 5) {
            String blobContainerName = parkingSpaceToUpdate.getBlobContainerName();
            // create blob container name and blob container if not exists
            if (StringUtils.isEmpty(blobContainerName) || blobContainerName.length() != 15) {
                boolean isBlobContainerCreated = false;
                try {
                    blobContainerName = "pictures" + RandomStringUtils.randomAlphabetic(7).toLowerCase();
                    isBlobContainerCreated = this.azureBlobService.createContainer(blobContainerName);
                } catch (URISyntaxException | StorageException exception) {
                    throw new RuntimeException(exception);
                }
                if (isBlobContainerCreated) {
                    parkingSpaceToUpdate.setBlobContainerName(blobContainerName);
                }
            }
            // upload file to azure blob storage
            try {
                URI blobFileUri = this.azureBlobService.uploadFile(multipartFile);
                parkingSpaceToUpdate.getPictureUris().add(blobFileUri.toString());
            } catch (URISyntaxException | StorageException | IOException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            throw new MaxCapacityReachedException();
        }
        return mapParkingSpaceToParkingSpaceResponse(this.parkingSpaceRepository.save(parkingSpaceToUpdate));
    }

    public ParkingSpaceResponse updateParkingSpace(User user, Long id, ParkingSpaceUpdateRequest request) {
        ParkingSpace parkingSpaceDao = this.parkingSpaceRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        if (!user.getMyParkingSpaces().contains(parkingSpaceDao)) {
            throw new NoPermissionException();
        }
        BeanUtils.copyProperties(request, parkingSpaceDao, ApplicationUtilities.getNullPropertyNames(request));
        if (parkingSpaceDao.getParkingLots().size() > request.getAmountOfParkingLots()) {
            for (int i = 0; i < parkingSpaceDao.getParkingLots().size() - request.getAmountOfParkingLots(); i++) {
                parkingSpaceDao.getParkingLots().add(new ParkingLot());
            }
        } else {
            if (request.getAmountOfParkingLots() > parkingSpaceDao.getParkingLots().size()) {
                // TODO cancel all bookings for this parking spaces
                parkingSpaceDao.getParkingLots().subList(parkingSpaceDao.getParkingLots().size(), request.getAmountOfParkingLots()).clear();
            }
        }
        return mapParkingSpaceToParkingSpaceResponse(this.parkingSpaceRepository.save(parkingSpaceDao));
    }

    public ParkingLot reserveParkingLot(ParkingSpace parkingSpace, LocalDateTime arrivalDateTime, LocalDateTime departureDateTime) {
        ParkingLot parkingLot = getAvailableParkingLots(parkingSpace, arrivalDateTime, departureDateTime).stream()
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);
        Timeslot timeslotToReserve = new Timeslot(arrivalDateTime, departureDateTime);
        parkingLot.getReservedTimeslots().add(timeslotToReserve);
        return this.parkingLotRepository.save(parkingLot);
    }

    public void cancelReservedParkingLot(Long parkingLotId, LocalDateTime arrivalDateTime, LocalDateTime departureDateTime) {
        ParkingLot parkingLot = this.parkingLotRepository.findById(parkingLotId)
                .orElseThrow(EntityNotFoundException::new);
        Timeslot timeslotToCancel = new Timeslot(arrivalDateTime, departureDateTime);
        parkingLot.getReservedTimeslots().remove(timeslotToCancel);
        this.parkingLotRepository.save(parkingLot);
    }

    // UTILITIES
    // Checks to see if Timeslot contains a specific date time.
    private boolean contains(Timeslot bookedTimeslot, LocalDateTime dateTime) {
        return dateTime.isAfter(bookedTimeslot.getStartingDateTime()) && dateTime.isBefore(bookedTimeslot.getEndingDateTime());
    }

    // Returns true if Timeslot overlaps another Timeslot.
    private boolean overlaps(Timeslot bookedTimeslot, LocalDateTime arrival, LocalDateTime departure) {
        return contains(bookedTimeslot, arrival) || contains(bookedTimeslot, departure);
    }

    // Returns true if at least one Timeslot is available.
    private boolean isAvailable(LocalDateTime arrival, LocalDateTime departure, List<Timeslot> bookedTimeslots) {
        if (bookedTimeslots.isEmpty()) {
            return true;
        }
        for (Timeslot timeslot : bookedTimeslots) {
            if (!overlaps(timeslot, arrival, departure)) {
                return true;
            }
        }
        return false;
    }

    private ParkingSpaceResponse mapParkingSpaceToParkingSpaceResponse(ParkingSpace parkingSpace) {
        ParkingSpaceResponse response = new ParkingSpaceResponse();
        BeanUtils.copyProperties(parkingSpace, response);
        response.setAmountOfParkingLots(parkingSpace.getParkingLots().size());
        return response;
    }

    private List<ParkingLot> getAvailableParkingLots(ParkingSpace parkingSpace, LocalDateTime arrival, LocalDateTime departure) {
        return parkingSpace.getParkingLots().stream()
                .filter(parkingLot -> isAvailable(arrival, departure, parkingLot.getReservedTimeslots()))
                .collect(Collectors.toList());
    }


    // EXCEPTIONS
    public static class NoPermissionException extends RuntimeException {
        public NoPermissionException() {
            super();
        }

        public NoPermissionException(String message) {
            super(message);
        }
    }

    public static class MaxCapacityReachedException extends RuntimeException {
        public MaxCapacityReachedException() {
            super();
        }

        public MaxCapacityReachedException(String message) {
            super(message);
        }
    }
}
