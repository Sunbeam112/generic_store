package ua.sunbeam.genericstore.api.model;


import jakarta.validation.constraints.*;

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
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")
    private String password;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
