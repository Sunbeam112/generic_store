package ua.sunbeam.genericstore.api.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponse {
    private String token;
    private boolean success;
    private String failureReason;

}
