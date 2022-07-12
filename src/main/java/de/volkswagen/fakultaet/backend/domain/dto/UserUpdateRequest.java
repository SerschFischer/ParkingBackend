package de.volkswagen.fakultaet.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String oldPassword;
    private String newPassword;
    private String firstname;
    private String lastname;
    private String street;
    private String houseNumber;
    private int zip;
    private String city;
}
