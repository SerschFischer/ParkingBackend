package de.volkswagen.fakultaet.backend.controller;

import de.volkswagen.fakultaet.backend.domain.model.AuthUser;
import de.volkswagen.fakultaet.backend.domain.model.Booking;
import de.volkswagen.fakultaet.backend.domain.model.BookingStatus;
import de.volkswagen.fakultaet.backend.domain.model.User;
import de.volkswagen.fakultaet.backend.service.AuthService;
import de.volkswagen.fakultaet.backend.service.AuthService.UnauthorizedException;
import de.volkswagen.fakultaet.backend.service.AuthService.TokenExpiredException;
import de.volkswagen.fakultaet.backend.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(Booking.class);
    private BookingService bookingService;
    private AuthService authService;

    public BookingController(BookingService bookingService, AuthService authService) {
        this.bookingService = bookingService;
        this.authService = authService;
    }

    @PostMapping("/request")
    public ResponseEntity<Booking> createBookingRequest(@RequestHeader(value = "Authorization") String token,
                                                        @RequestParam(name = "parking_space_id", required = true) Long parkingSpaceId,
                                                        @RequestParam(name = "arrival_date_time", required = true) LocalDateTime arrivalDateTime,
                                                        @RequestParam(name = "departure_date_time", required = true) LocalDateTime departureDateTime) {
        try {
            User tenant = this.authService.getCurrentUser(token);
            return ResponseEntity.accepted().body(this.bookingService.
                    createBookingRequest(tenant.getId(), parkingSpaceId, arrivalDateTime, departureDateTime));
        } catch (UnauthorizedException | TokenExpiredException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/request")
    public ResponseEntity<List<Booking>> getBookingRequests(@RequestHeader(value = "Authorization") String token) {
        try {
            Long ownerId = this.authService.getCurrentUser(token).getId();
            return ResponseEntity.accepted().body(this.bookingService.getUpcomingBookingRequest(ownerId));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        } catch (TokenExpiredException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/response")
    public ResponseEntity<Booking> createBookingResponse(@RequestHeader(value = "Authorization") String token,
                                                         @RequestParam(name = "booking_id", required = true) Long bookingId,
                                                         @RequestParam(name = "booking_status", required = true) BookingStatus bookingStatus) {
        try {
            Long ownerId = this.authService.getCurrentUser(token).getId();
            return ResponseEntity.accepted().body(this.bookingService.createBookingResponse(ownerId, bookingId, bookingStatus));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        } catch (TokenExpiredException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/response")
    public ResponseEntity<List<Booking>> getBookingResponses(@RequestHeader(value = "Authorization") String token) {
        try {
            Long tenantId = this.authService.getCurrentUser(token).getId();
            return ResponseEntity.accepted().body(this.bookingService.getUpcomingBookingResponses(tenantId));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        } catch (TokenExpiredException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<List<String>> getBookingStatus() {
        return ResponseEntity.ok(this.bookingService.getAllStatusAsString());
    }
}
