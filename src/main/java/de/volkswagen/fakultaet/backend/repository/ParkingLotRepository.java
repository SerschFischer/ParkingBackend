package de.volkswagen.fakultaet.backend.repository;

import de.volkswagen.fakultaet.backend.domain.model.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
}
