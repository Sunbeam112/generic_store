package ua.sunbeam.genericstore.api.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordResetRequestBody {
    @NotNull
    @NotBlank
    @Size(min = 8, max = 64)
    private String newPassword;
    @NotBlank
    @NotNull
    private String token;


}
