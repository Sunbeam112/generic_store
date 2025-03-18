package ua.sunbeam.genericstore.api.model;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegistrationBody {
    @Email
    @NotNull
    @NotBlank
    @NotEmpty
    @Size(min = 4, max = 320)

    private String email;

    @NotNull
    @NotBlank
    @NotEmpty
    @Size(min = 8, max = 32)
    //TODO: ADD REGEX PATTERN TO PASSWORD
    /*@Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")*/
    private String password;


}
