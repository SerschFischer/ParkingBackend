package de.volkswagen.fakultaet.backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "PARKING_SPACES")
@JsonIgnoreProperties({"parkingLots", "user", "blobContainerName"})
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ParkingSpace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private String parkingDescription;
    private String accessInformation;
    private Double pricePerHour;
    @OneToMany(targetEntity = ParkingLot.class,
            fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST})
    private List<ParkingLot> parkingLots;

    private String blobContainerName;
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_PARKING_SPACE_ID", referencedColumnName = "id")
    private User user;
    @ElementCollection
    @CollectionTable(name = "PICTURES", joinColumns = @JoinColumn(name = "PICTURE_ID"))
    @Column(name = "URI")
    private List<String> pictureUris;

    @Override
    public String toString() {
        return "ParkingSpace{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", parkingDescription='" + parkingDescription + '\'' +
                ", accessInformation='" + accessInformation + '\'' +
                ", pricePerHour=" + pricePerHour +
                '}';
    }
}


