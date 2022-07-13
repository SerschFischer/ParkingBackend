package de.volkswagen.fakultaet.backend.service;

import com.microsoft.azure.storage.StorageException;
import de.volkswagen.fakultaet.backend.domain.dto.ParkingSpaceRequest;
import de.volkswagen.fakultaet.backend.domain.dto.ParkingSpaceResponse;
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

    public ParkingSpaceResponse createParkingSpace(ParkingSpaceRequest request, User user) {
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

    public ParkingSpaceResponse getParkingSpaceResponseById(Long id) {
        return mapParkingSpaceToParkingSpaceResponse(this.parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID does not exists")));
    }

    public ParkingSpace getParkingSpaceById(Long id) {
        return this.parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID does not exists"));
    }
    public List<ParkingSpaceResponse> getParkingSpacesByUser(User user) {
        return this.parkingSpaceRepository.findByUser(user).stream()
                .map(parkingSpace -> mapParkingSpaceToParkingSpaceResponse(parkingSpace))
                .collect(Collectors.toList());
    }

    public List<ParkingSpaceResponse> getAvailableParkingSpacesBySearchingByCriteria(String location, LocalDateTime arrival, LocalDateTime departure) {
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
        if (!currentUser.getParkingSpaces().contains(parkingSpaceToUpdate)) {
            throw new NoPermissionException();
        }
        if (parkingSpaceToUpdate.getPictureUris().size() < 5) {
            String blobContainerName = parkingSpaceToUpdate.getBlobContainerName();
            // create blob container name and blob container if not exists
            if (StringUtils.isEmpty(blobContainerName)) {
                boolean isBlobContainerCreated = false;
                try {
                    blobContainerName = "pictures-" + RandomStringUtils.randomAlphabetic(11).toLowerCase();
                    isBlobContainerCreated = this.azureBlobService.createContainer(blobContainerName);
                } catch (URISyntaxException | StorageException exception) {
                    throw new RuntimeException("Blob container could not creates.");
                }
                if (isBlobContainerCreated) {
                    parkingSpaceToUpdate.setBlobContainerName(blobContainerName);
                }
            }
            // upload file to azure blob storage
            try {
                URI blobFileUri = this.azureBlobService.uploadFile(blobContainerName, multipartFile);
                parkingSpaceToUpdate.getPictureUris().add(blobFileUri.toString());
            } catch (URISyntaxException | StorageException | IOException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            throw new MaxCapacityReachedException("Max Capacity is reached.");
        }
        return mapParkingSpaceToParkingSpaceResponse(this.parkingSpaceRepository.save(parkingSpaceToUpdate));
    }

    public ParkingSpaceResponse updateParkingSpace(User user, Long id, ParkingSpaceRequest request) {
        ParkingSpace parkingSpaceDao = this.parkingSpaceRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        if (!user.getParkingSpaces().contains(parkingSpaceDao)) {
            throw new NoPermissionException();
        }
        BeanUtils.copyProperties(request, parkingSpaceDao, ApplicationUtilities.getNullPropertyNames(request));
        if (parkingSpaceDao.getParkingLots().size() > request.getAmountOfParkingLots()) {
            for (int i = 0; i < parkingSpaceDao.getParkingLots().size() - request.getAmountOfParkingLots(); i++) {
                parkingSpaceDao.getParkingLots().add(new ParkingLot());
            }
        } else {
            if (request.getAmountOfParkingLots() > parkingSpaceDao.getParkingLots().size()) {
                parkingSpaceDao.getParkingLots().subList(parkingSpaceDao.getParkingLots().size(), request.getAmountOfParkingLots()).clear();
            }
        }
        return mapParkingSpaceToParkingSpaceResponse(this.parkingSpaceRepository.save(parkingSpaceDao));
    }

    public ParkingLot reserveParkingLot(ParkingSpace parkingSpace, LocalDateTime arrivalDateTime, LocalDateTime departureDateTime) {
        ParkingLot parkingLot = getAvailableParkingLots(parkingSpace, arrivalDateTime, departureDateTime).stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No available parking lot was found"));
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
    private boolean endsAtTheSameTimeAs(Timeslot bookedTimeslot, LocalDateTime dateTimeToCheck) {
        return bookedTimeslot.getEndingDateTime().isEqual(dateTimeToCheck);
    }

    private boolean startsAtTheSameTimeAs(Timeslot bookedTimeslot, LocalDateTime dateTimeToCheck) {
        return bookedTimeslot.getStartingDateTime().isEqual(dateTimeToCheck);
    }

    private boolean equals(Timeslot bookedTimeslot, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return startsAtTheSameTimeAs(bookedTimeslot, startDateTime) && endsAtTheSameTimeAs(bookedTimeslot, endDateTime);
    }

    private boolean contains(Timeslot bookedTimeslot, LocalDateTime dateTimeToCheck) {
        return dateTimeToCheck.isAfter(bookedTimeslot.getStartingDateTime()) || dateTimeToCheck.isBefore(bookedTimeslot.getEndingDateTime());
    }

    private boolean overlaps(Timeslot bookedTimeslot, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return contains(bookedTimeslot, startDateTime) || contains(bookedTimeslot, endDateTime);
    }

    private boolean isAvailable(List<Timeslot> bookedTimeslots, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (bookedTimeslots == null || bookedTimeslots.size() == 0) {
            return true;
        }
        for (Timeslot timeslotToCheck : bookedTimeslots) {
            if (overlaps(timeslotToCheck, startDateTime, endDateTime) || equals(timeslotToCheck, startDateTime, endDateTime)) {
                return false;
            }
        }
        return true;
    }

    private ParkingSpaceResponse mapParkingSpaceToParkingSpaceResponse(ParkingSpace parkingSpace) {
        ParkingSpaceResponse response = new ParkingSpaceResponse();
        BeanUtils.copyProperties(parkingSpace, response);
        response.setAmountOfParkingLots(parkingSpace.getParkingLots().size());
        return response;
    }

    private List<ParkingLot> getAvailableParkingLots(ParkingSpace parkingSpace, LocalDateTime arrivalDateTime, LocalDateTime departureDateTime) {
        return parkingSpace.getParkingLots().stream()
                .filter(parkingLot -> isAvailable(parkingLot.getReservedTimeslots(), arrivalDateTime, departureDateTime))
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
