package de.volkswagen.fakultaet.backend.service;

import de.volkswagen.fakultaet.backend.domain.model.*;
import de.volkswagen.fakultaet.backend.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookingService.class);
    private BookingRepository bookingRepository;
    private ParkingService parkingService;

    public BookingService(BookingRepository bookingRepository, ParkingService parkingService) {
        this.bookingRepository = bookingRepository;
        this.parkingService = parkingService;
    }

    public Booking createBookingRequest(Long tenantId, Long parkingSpaceId, LocalDateTime arrivalDateTime, LocalDateTime departureDateTime) {
        ParkingSpace parkingSpaceToBook = this.parkingService.getParkingSpaceById(parkingSpaceId);
        Booking bookingToAdd = new Booking();
        bookingToAdd.setOwnerId(parkingSpaceToBook.getUser().getId());
        bookingToAdd.setTenantId(tenantId);
        bookingToAdd.setParkingSpaceId(parkingSpaceId);
        // get first available parking lot and set it to booking entity
        bookingToAdd.setParkingLotId(this.parkingService.reserveParkingLot(parkingSpaceToBook, arrivalDateTime, departureDateTime).getId());
        bookingToAdd.setArrivalDateTime(arrivalDateTime);
        bookingToAdd.setDepartureDateTime(departureDateTime);
        // calculate total price for booking request
        double totalPrice = ((double) ChronoUnit.MINUTES.between(arrivalDateTime, departureDateTime)) * parkingSpaceToBook.getPricePerHour() / 60;
        bookingToAdd.setTotalPrice(totalPrice);
        bookingToAdd.setBookingStatus(BookingStatus.REQUESTED);
        return this.bookingRepository.save(bookingToAdd);
    }

    public List<Booking> getUpcomingBookingRequest(Long ownerId) {
        return this.bookingRepository.findByOwnerIdAndBookingStatus(ownerId, BookingStatus.REQUESTED);
    }

    public Booking createBookingResponse(Long ownerId, Long bookingId, BookingStatus bookingStatus) {
        Booking bookingResponse = this.bookingRepository.findById(bookingId)
                .orElseThrow(EntityNotFoundException::new);
        if (this.bookingRepository.existsByOwnerId(ownerId)) {
            bookingResponse.setBookingStatus(bookingStatus);
            if (bookingStatus.equals(BookingStatus.REJECTED)) {
                this.parkingService.cancelReservedParkingLot(bookingResponse.getParkingLotId(),
                        bookingResponse.getArrivalDateTime(),
                        bookingResponse.getDepartureDateTime());
            }
            return this.bookingRepository.save(bookingResponse);
        } else {
            throw new EntityNotFoundException();
        }
    }

    public List<Booking> getUpcomingBookingResponses(Long tenantId) {
        List<Booking> bookingRequests = this.bookingRepository.findByTenantId(tenantId);
        return bookingRequests.stream()
                .filter(booking -> !(booking.getBookingStatus().equals(BookingStatus.REQUESTED)))
                .collect(Collectors.toList());
    }

    public List<String> getAllStatusAsString() {
        return Arrays.stream(BookingStatus.values()).map(Objects::toString).collect(Collectors.toList());
    }
}
