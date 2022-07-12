package de.volkswagen.fakultaet.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {
    private String email;
    private String password;
}
