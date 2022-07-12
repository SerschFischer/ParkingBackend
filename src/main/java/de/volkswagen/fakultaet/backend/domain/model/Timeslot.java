package de.volkswagen.fakultaet.backend.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Timeslot {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startingDateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endingDateTime;
}
