package de.volkswagen.fakultaet.backend;

import de.volkswagen.fakultaet.backend.domain.dto.ParkingSpaceCreateRequest;
import de.volkswagen.fakultaet.backend.domain.dto.UserRegisterRequest;
import de.volkswagen.fakultaet.backend.domain.model.ParkingSpace;
import de.volkswagen.fakultaet.backend.domain.model.User;
import de.volkswagen.fakultaet.backend.service.AuthService;
import de.volkswagen.fakultaet.backend.service.ParkingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner run(AuthService authService, ParkingService parkingService) {
        return args -> {
            // REGISTER DUMMY USER PETER
            UserRegisterRequest peter = new UserRegisterRequest();
            peter.setEmail("peter@dummy.de");
            peter.setPassword("supersave");

            String petersToken = authService.registerUser(peter);
            LOGGER.info("token of current user - peter: {}", petersToken);

            User currentUserPeter = authService.getCurrentUser(petersToken);
            LOGGER.info("current user - peter: {}", currentUserPeter);

            // CREATE DUMMY PARKING SPACE
            ParkingSpaceCreateRequest dummyParkingSpaceCreateBerlin = new ParkingSpaceCreateRequest();
            dummyParkingSpaceCreateBerlin.setName("Blaaa");
            dummyParkingSpaceCreateBerlin.setLocation("Kantstr. 4, 10625 Berlin");
            dummyParkingSpaceCreateBerlin.setParkingDescription("some parking description");
            dummyParkingSpaceCreateBerlin.setAccessInformation("some access information");
            dummyParkingSpaceCreateBerlin.setPricePerHour(1.5);
            dummyParkingSpaceCreateBerlin.setAmountOfParkingLots(3);
            LOGGER.info("dummyParkingSpaceCreateBerlin: {}", dummyParkingSpaceCreateBerlin);

            LOGGER.info("dummyParkingSpaceBerlin: {}", parkingService.createParkingSpace(dummyParkingSpaceCreateBerlin, currentUserPeter));

            // REGISTER DUMMY USER DIETER
            UserRegisterRequest dieter = new UserRegisterRequest();
            dieter.setEmail("dieter@dummy.de");
            dieter.setPassword("not-so-save");

            String dietersToken = authService.registerUser(dieter);
            LOGGER.info("token of current user - peter: {}", dietersToken);

            User currentUserDieter = authService.getCurrentUser(dietersToken);
            LOGGER.info("current user - dieter: {}", currentUserDieter);

            ParkingSpaceCreateRequest dummyParkingSpaceCreateKassel = new ParkingSpaceCreateRequest();
            dummyParkingSpaceCreateKassel.setName("Private");
            dummyParkingSpaceCreateKassel.setLocation("Goethestr. 4, 34119 Kassel");
            dummyParkingSpaceCreateKassel.setParkingDescription("some parking description");
            dummyParkingSpaceCreateKassel.setAccessInformation("some access information");
            dummyParkingSpaceCreateKassel.setPricePerHour(3.5);
            dummyParkingSpaceCreateKassel.setAmountOfParkingLots(7);
            LOGGER.info("dummyParkingSpaceCreateKassel: {}", dummyParkingSpaceCreateBerlin);

            LOGGER.info("dummyParkingSpaceKassel: {}", parkingService.createParkingSpace(dummyParkingSpaceCreateKassel, currentUserDieter));
        };
    }
}
