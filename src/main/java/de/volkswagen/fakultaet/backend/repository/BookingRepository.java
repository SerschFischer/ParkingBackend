package de.volkswagen.fakultaet.backend.repository;

import de.volkswagen.fakultaet.backend.domain.model.Booking;
import de.volkswagen.fakultaet.backend.domain.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByTenantId(Long tenantId);

    List<Booking> findByOwnerIdAndBookingStatus(Long ownerId, BookingStatus bookingStatus);

    boolean existsByOwnerId(Long ownerId);
}
