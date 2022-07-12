package de.volkswagen.fakultaet.backend.repository;

import de.volkswagen.fakultaet.backend.domain.model.ParkingSpace;
import de.volkswagen.fakultaet.backend.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    List<ParkingSpace> findByLocationContainsIgnoreCase(String city);

    List<ParkingSpace> findByUser(User user);
}
