package de.volkswagen.fakultaet.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String street;
    private String houseNumber;
    private int zip;
    private String city;
}
