package de.volkswagen.fakultaet.backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "USERS")
@JsonIgnoreProperties({"password", "myParkingSpaces"})
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String street;
    private String houseNumber;
    private int zip;
    private String city;
    @OneToMany(targetEntity = ParkingSpace.class,
            mappedBy = "user",
            fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST})
    private List<ParkingSpace> myParkingSpaces;
}
