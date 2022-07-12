package de.volkswagen.fakultaet.backend.repository;

import de.volkswagen.fakultaet.backend.domain.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByToken(String token);
}
