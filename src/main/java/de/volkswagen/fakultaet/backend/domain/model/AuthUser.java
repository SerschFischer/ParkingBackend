package de.volkswagen.fakultaet.backend.domain.model;

import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LOGGED_USERS")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    @Column(name = "USER_ID", nullable = false)
    private Long userId;
    private LocalDateTime expiredAt;

    @PrePersist
    @PreUpdate
    private void setExpiredAt() {
        this.token = RandomStringUtils.randomAlphanumeric(73);
        this.expiredAt = LocalDateTime.now().plusHours(4);
    }
}
