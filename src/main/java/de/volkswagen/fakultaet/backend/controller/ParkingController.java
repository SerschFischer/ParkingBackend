package de.volkswagen.fakultaet.backend.controller;

import com.sun.istack.NotNull;
import de.volkswagen.fakultaet.backend.domain.dto.ParkingSpaceResponse;
import de.volkswagen.fakultaet.backend.domain.dto.ParkingSpaceRequest;
import de.volkswagen.fakultaet.backend.domain.model.User;
import de.volkswagen.fakultaet.backend.service.AuthService;
import de.volkswagen.fakultaet.backend.service.AuthService.TokenExpiredException;
import de.volkswagen.fakultaet.backend.service.AuthService.UnauthorizedException;
import de.volkswagen.fakultaet.backend.service.ParkingService;
import de.volkswagen.fakultaet.backend.service.ParkingService.NoPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@RestController()
@RequestMapping("/api/parking")
public class ParkingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingController.class);
    private ParkingService parkingService;
    private AuthService authService;

    public ParkingController(ParkingService parkingService, AuthService authService) {
        this.parkingService = parkingService;
        this.authService = authService;
    }


    //CRUD
    @PostMapping("/spaces")
    public ResponseEntity<ParkingSpaceResponse> createParkingSpace(@RequestHeader(value = "Authorization") String token,
                                                                   @RequestBody ParkingSpaceRequest parkingSpaceCreateRequest) {
        try {
            User currentUser = this.authService.getCurrentUser(token);
            return ResponseEntity.ok(this.parkingService.createParkingSpace(parkingSpaceCreateRequest, currentUser));
        } catch (UnauthorizedException | TokenExpiredException | EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/spaces/searching")
    public ResponseEntity<List<ParkingSpaceResponse>> getParkingSpaces(@RequestParam(name = "location") String location,
                                                                       @RequestParam(name = "arrival_date_time") LocalDateTime arrivalDateTime,
                                                                       @RequestParam(name = "departure_date_time") LocalDateTime departureDateTime) {
        List<ParkingSpaceResponse> parkingSpaceResponses = parkingService.getAvailableParkingSpacesBySearchingByCriteria(location, arrivalDateTime, departureDateTime);
        return parkingSpaceResponses.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(parkingSpaceResponses);
    }

    @GetMapping("/spaces/searching/id")
    public ResponseEntity<ParkingSpaceResponse> getParkingSpaceById(@RequestParam(name = "parking_space_id") Long parkingSpaceId) {
        try {
            return ResponseEntity.ok(this.parkingService.getParkingSpaceResponseById(parkingSpaceId));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/spaces/searching/user")
    public ResponseEntity<List<ParkingSpaceResponse>> getParkingSpacesByUser(@RequestHeader(value = "Authorization") String token) {
        try {
            User currentUser = this.authService.getCurrentUser(token);
            return ResponseEntity.ok(this.parkingService.getParkingSpacesByUser(currentUser));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        } catch (TokenExpiredException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PatchMapping("/spaces/searching/id")
    public ResponseEntity<ParkingSpaceResponse> updateParkingSpace(@RequestHeader(value = "Authorization") String token,
                                                                   @RequestParam(name = "parking_space_id") Long parkingSpaceId,
                                                                   @RequestBody ParkingSpaceRequest request) {
        try {
            User currentUser = this.authService.getCurrentUser(token);
            return ResponseEntity.ok(this.parkingService.updateParkingSpace(currentUser, parkingSpaceId, request));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        } catch (TokenExpiredException | NoPermissionException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


    }

    @PatchMapping("/spaces/searching/id/picture")
    public ResponseEntity<ParkingSpaceResponse> addPictureToParkingSpace(@RequestHeader(value = "Authorization") String token,
                                                                         @RequestParam(name = "parking_space_id") Long parkingSpaceId,
                                                                         @RequestParam(name = "file") @NotNull MultipartFile multipartFile) {
        try {
            User currentUser = this.authService.getCurrentUser(token);
            return ResponseEntity.ok(this.parkingService.addPictureToParkingSpace(currentUser, parkingSpaceId, multipartFile));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        } catch (TokenExpiredException | NoPermissionException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
